package com.app.ant.app.ServiceLayer;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import com.app.ant.R;
import com.app.ant.app.BusinessLayer.Document;
import com.app.ant.app.BusinessLayer.Plans;
import com.app.ant.app.DataLayer.Db;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class Synchronizer extends AsyncTask<Void, Integer, Long> {
    public static final Integer SYNC_TYPE_NONE = 0;
    public static final Integer SYNC_TYPE_FULL = 1;
    public static final Integer SYNC_TYPE_INCR = 2;
    public static final Integer SYNC_TYPE_SALDO = 4;
    public static final Integer SYNC_TYPE_REST = 8;
    public static final Integer SYNC_TYPE_AUTH = 16;
    public static final Integer SYNC_TYPE_FULL_SEND = 32;
    public static final Integer SYNC_TYPE_INCR_SEND = 64;
    public static final Integer SYNC_TYPE_MEDIA = 128;
    public static final Integer SYNC_TYPE_UPDATE = 256;

    public static final Integer SYNC_TYPE_MEDIA_SEND = 256;
    public static final String DB_PATH = "/data/data/com.app.ant/databases/";
    public static final String FILES_PATH = "/data/data/com.app.ant/files/";
    public static final String SD_FILES_PATH = "/sdcard/data/data/com.app.ant/files/";
    public static final String SD_FILES_EXPORT_PATH = "/sdcard/data/data/com.app.ant/files/EXPORT/";
    public static final String DB_NAME = "ant.db";
    public static final String DB_NAME_BAK = "ant_bak.db";
    public static final String DB_NAME_ZIP_TMP = "ant_db_tmp.zip";
    public static final String DB_NAME_TMP2 = "ant_db_tmp2.db";
    public static final String FILES_NAME_ZIP = "ant_files.zip";

    private boolean bHaveChanges = false;
    private String syncError = "";
    private String apkUrl;

    //Asynk task params
    private boolean bSend;
    private boolean bRecieve;
    private boolean bUpdate;
    private boolean bMediaFiles;

    private Integer sendType;
    private Integer recieveType;
    private String serverUrl;

    private ProgressDialog progressDialog;
    private Context context;

    private Integer sendFileSize = 0;
    private Integer recieveFileSize = 0;

    private String[] progressMessages = {"Connecting ... ", "Creating DB ... ", "Transfer data ... ", "Closing connect ... ", "Processing data ... "};

    //-------------------------------------------------------------------------------------------------------

    /**
     * Callback: �������� ��������� ���������� �������������.
     */
    public interface ISynchronizationResult {
        abstract void onSynchronizationFinished(boolean bHaveDbChanges, String msg, String apkUrl);
    }

    public Synchronizer(Context context) {
        this.context = context;
        //localization
        progressMessages[0] = context.getResources().getString(R.string.sync_connecting);
        progressMessages[1] = context.getResources().getString(R.string.sync_db_creating);
        progressMessages[2] = context.getResources().getString(R.string.sync_transfer_data);
        progressMessages[3] = context.getResources().getString(R.string.sync_close_connection);
        progressMessages[4] = context.getResources().getString(R.string.sync_processing_data);
    }

    //-----------------------------------------------------------------	
    public void StartAsyncSyncronizeTask(Integer syncType, String serverUrl) throws Exception {
        bSend = (syncType & (SYNC_TYPE_FULL_SEND | SYNC_TYPE_INCR_SEND | SYNC_TYPE_MEDIA_SEND)) > 0;
        bRecieve = (syncType & (SYNC_TYPE_FULL | SYNC_TYPE_INCR | SYNC_TYPE_REST | SYNC_TYPE_SALDO | SYNC_TYPE_MEDIA)) > 0;
        bUpdate = syncType == SYNC_TYPE_UPDATE;
        bMediaFiles = (syncType & (SYNC_TYPE_MEDIA | SYNC_TYPE_MEDIA_SEND)) > 0;
        bHaveChanges = false;

        this.sendType = syncType & (SYNC_TYPE_FULL_SEND | SYNC_TYPE_INCR_SEND);
        this.recieveType = syncType & (SYNC_TYPE_FULL | SYNC_TYPE_INCR | SYNC_TYPE_REST | SYNC_TYPE_SALDO);

        this.serverUrl = serverUrl;

        this.execute();
    }

    // --------------------------------------------------------------
    public void DisplayProgressDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Connecting ...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    // --------------------------------------------------------------
    public void DismissProgressDialog() {
        progressDialog.dismiss();
    }

    // --------------------------------------------------------------
    protected void onPreExecute() {
        DisplayProgressDialog();
    }

    // --------------------------------------------------------------
    protected void onProgressUpdate(Integer... params) {
        String msg = progressMessages[params[0]];
        int totalCnt = params[1];
        int currentCnt = params[2];

        if (totalCnt > 0) {
            int percentage = (int) (((float) currentCnt / (float) totalCnt) * 100);
            progressDialog.setMessage(msg + percentage + "%");
        } else {
            progressDialog.setMessage(msg);
        }
    }

    // --------------------------------------------------------------
    protected Long doInBackground(Void... params) {
        try {
            syncError = "";
            apkUrl = "";
            String lastSyncDate = context.getResources().getString(R.string.preference_key_last_sync_date);

            String lastSyncSizeSend = context.getResources().getString(R.string.preference_key_last_sync_size_send);
            String lastSyncSizeRecieve = context.getResources().getString(R.string.preference_key_last_sync_size_recieve);

            String monthSyncSizeSend = context.getResources().getString(R.string.preference_key_month_sync_size_send);
            String monthSyncSizeRecieve = context.getResources().getString(R.string.preference_key_month_sync_size_recieve);

            if (bSend | bRecieve) {
                Date date = new Date();
                Date lastDate = new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    lastDate = df.parse(Settings.getInstance().getStringSyncPreference(lastSyncDate, df.format(date)));
                } catch (Exception ex) {
                    ErrorHandler.CatchError("doInBackground::df.parse ", ex);
                }
                if (date.getMonth() != lastDate.getMonth()) {
                    Settings.getInstance().setStringSyncPreference(monthSyncSizeSend, "0");
                    Settings.getInstance().setStringSyncPreference(monthSyncSizeRecieve, "0");
                }

                Settings.getInstance().setStringSyncPreference(lastSyncDate, df.format(date));
            }
            if (bSend && !bMediaFiles) {
                String serverUrlSend = serverUrl + getConnectParameters(this.sendType);
                sendDatabase(this.sendType == SYNC_TYPE_FULL_SEND, serverUrlSend);

                Settings.getInstance().setStringSyncPreference(lastSyncSizeSend, sendFileSize.toString());

                Double size = Convert.toDouble(Settings.getInstance().getStringSyncPreference(monthSyncSizeSend, "0"), 0.0) + sendFileSize;
                Settings.getInstance().setStringSyncPreference(monthSyncSizeSend, size.toString());
            }
            if (bRecieve && !bMediaFiles) {
                if (!(bSend && !bHaveChanges)) {
                    String serverUrlRecieve = serverUrl + getConnectParameters(this.recieveType);
                    receiveDatabase(this.recieveType == SYNC_TYPE_FULL, serverUrlRecieve);

                    Settings.getInstance().setStringSyncPreference(lastSyncSizeRecieve, recieveFileSize.toString());

                    Double size = Convert.toDouble(Settings.getInstance().getStringSyncPreference(monthSyncSizeRecieve, "0"), 0.0) + recieveFileSize;
                    Settings.getInstance().setStringSyncPreference(monthSyncSizeRecieve, size.toString());
                }
            }
            if (bUpdate) {
                apkUrl = receiveUpdateApk(serverUrl + getConnectParameters(SYNC_TYPE_UPDATE,true));
            }
            if (bMediaFiles) {
                if (bSend) {
                    sendMediaFiles(serverUrl + getConnectParameters(SYNC_TYPE_MEDIA_SEND));
                }
                if (bRecieve) {
                    recieveFiles(serverUrl + getConnectParameters(SYNC_TYPE_MEDIA));

                    Settings.getInstance().setStringSyncPreference(lastSyncSizeRecieve, recieveFileSize.toString());
                    Double size = Convert.toDouble(Settings.getInstance().getStringSyncPreference(monthSyncSizeRecieve, "0"), 0.0) + recieveFileSize;
                    Settings.getInstance().setStringSyncPreference(monthSyncSizeRecieve, size.toString());
                }
            }

            Settings.getInstance().setNeedReinit(true);

            checkAndCreateMediaPath(SD_FILES_PATH);

        } catch (Exception ex) {
            ErrorHandler.CatchError("Exception in Synchronizer::doInBackground", ex);
            syncError = ex.getLocalizedMessage();
        }
        return null;
    }

    protected void onPostExecute(Long unused) {
        Plans.recalcFacts();

        DismissProgressDialog();

        if (context instanceof ISynchronizationResult)
            ((ISynchronizationResult) context).onSynchronizationFinished(bHaveChanges, syncError, apkUrl);

        super.onPostExecute(unused);
    }
    //--------------------------------------------------------------

    /**
     * Method to send file to anthill
     * Something like this
     * TESTED! WORKED!
     *
     * @param fileName  - file name in directory `data`
     * @param urlServer
     */
    public boolean fileTransfer(String fileName, boolean bSend, FileInputStream inputStream, FileOutputStream outputStream, String urlServer) throws Exception {
        HttpURLConnection connection = null;
        String boundary = "*****";
        //Responses from the server (code and message)
        int serverResponseCode = HttpURLConnection.HTTP_BAD_REQUEST;
        String serverResponseMessage = "";

        try {
            publishProgress(0, 0, 0);

            URL url = new URL(urlServer);
            Log.v("den", urlServer);
            connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs & Outputs
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Enable POST method
            if (bSend == true)
                connection.setRequestMethod("POST");
            else
                connection.setRequestMethod("GET");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            int contentLen = 0;
            int contentDone = 0;

            if (bSend) {
                String lineEnd = "\r\n";
                String twoHyphens = "--";

                //send file to server
                int bytesRead, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1024 * 50; //50 KB

                DataOutputStream connOutputStream = null;
                connOutputStream = new DataOutputStream(connection.getOutputStream());
                connOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                connOutputStream.writeBytes("Content-Disposition: form-data; name=\"ant.db\";filename=\"" + fileName + "\"" + lineEnd);
                connOutputStream.writeBytes(lineEnd);

                int bytesAvailable = inputStream.available();

                contentLen = bytesAvailable;
                sendFileSize = bytesAvailable;

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = inputStream.read(buffer, 0, bufferSize); // Read file

                while (bytesRead > 0) {
                    contentDone += bufferSize;
                    connOutputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = inputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = inputStream.read(buffer, 0, bufferSize);
                    publishProgress(2, contentLen, contentDone - 1024);
                }

                connOutputStream.writeBytes(lineEnd);
                connOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                connOutputStream.flush();
                connOutputStream.close();
                publishProgress(2, contentLen, contentDone);
            } else {
                int bufferSize = 1024;
                byte[] bufferInput = new byte[bufferSize];

                InputStream connInputStream = connection.getInputStream();
                BufferedInputStream bInStream = new BufferedInputStream(connInputStream);

                contentLen = connection.getContentLength();

                int nBytes = 0;

                while ((nBytes = bInStream.read(bufferInput, 0, bufferSize)) > 0) {
                    try {
                        contentDone += nBytes;
                        outputStream.write(bufferInput, 0, nBytes);
                        publishProgress(2, contentLen, contentDone);
                    } catch (IOException ex) {
                        break;
                    }
                }
                recieveFileSize = contentDone;
                publishProgress(2, contentLen, contentLen);
            }
            if (connection != null) {
                serverResponseCode = connection.getResponseCode();
                serverResponseMessage = connection.getResponseMessage();
            }
        } catch (Exception ex) {
            throw new Exception(serverResponseMessage + ". " + ex); //Exception handling			
        } finally {
            publishProgress(3, 0, 0);
            if (connection != null) connection.disconnect();
        }
        return serverResponseCode == HttpURLConnection.HTTP_OK;
    }

    //-----------------------------------------------------------------
    public void sendDatabase(boolean bFull, String serverUrl) throws Exception {
        boolean isTransfer = false;
        String pathToLocalFile = DB_PATH + DB_NAME;

        publishProgress(1, 0, 0);

        //File local = new File(pathToLocalFile);
        InputStream inputStream = new FileInputStream(pathToLocalFile);

        String pathToLocalBackupFile = DB_PATH + DB_NAME_BAK;
        File backupDb = new File(pathToLocalBackupFile);
        if (backupDb.exists()) backupDb.delete();
        copyDataBase(inputStream, pathToLocalBackupFile);

        inputStream.close();
        inputStream = null;

        backupDb = new File(pathToLocalBackupFile);
        if (!backupDb.exists()) return;

        try {
            publishProgress(1, 10, 1);

            truncateEntitiesNotSend(DB_NAME_BAK);

            publishProgress(1, 10, 7);

            String pathToLocalArchive = DB_PATH + DB_NAME_ZIP_TMP;
            Archivator.zipFile(pathToLocalBackupFile, pathToLocalArchive);

            publishProgress(1, 10, 9);

            File arch = new File(pathToLocalArchive);
            FileInputStream fileInputStream = new FileInputStream(arch);

            publishProgress(1, 10, 10);

            isTransfer = fileTransfer(DB_NAME_ZIP_TMP, true, fileInputStream, null, serverUrl);

            fileInputStream.close();
            arch.delete();
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (isTransfer) {
                publishProgress(3, 2, 1);
                markEntitiesSended();
                publishProgress(3, 2, 2);
            }
        }
        bHaveChanges = isTransfer;
    }

    private void sendMediaFiles(String serverUrl) throws Exception {
        File exportPath = new File(SD_FILES_EXPORT_PATH);
        if (exportPath.exists()) {
            sendMediaFile(exportPath, serverUrl);
        }
        bHaveChanges = true;
    }

    private void sendMediaFile(File file, String serverUrl) throws Exception {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file2 : files) {
                sendMediaFile(file2, serverUrl);
            }
        } else {
            FileInputStream fileInputStream;
            try {
                if (!file.getName().startsWith("_")) {
                    fileInputStream = new FileInputStream(file);
                    fileTransfer(file.getName(), true, fileInputStream, null, serverUrl);
                    file.renameTo(new File(file.getParent() + File.separator + "_" + file.getName()));
                }
            } catch (FileNotFoundException ex) {
                throw ex;
            }
        }
    }

    //-----------------------------------------------------------------
    private void receiveDatabase(boolean bFull, String serverUrl) throws Exception {
        //download a file to temporary location
        FileOutputStream fileOutStream = context.openFileOutput(DB_NAME_ZIP_TMP, 0);
        boolean isTransfer = fileTransfer(DB_NAME, false, null, fileOutStream, serverUrl);
        fileOutStream.flush();
        fileOutStream.close();

        publishProgress(4, 4, 0);

        if (bFull) {
//			File inFile = context.getFileStreamPath (DB_NAME_ZIP_TMP); //unzip temporary file to database location
//			String pathToLocalFile = DB_PATH + DB_NAME;			
//			Archivator.unZipFile(inFile.getPath(), pathToLocalFile);

            File inFile = context.getFileStreamPath(DB_NAME_ZIP_TMP); //unzip temporary file to database location			
            publishProgress(4, 4, 1);
            ZipFile zipFile = new ZipFile(inFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            publishProgress(4, 4, 2);
            if (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                Db.getInstance().close();
                copyDataBase(zipFile.getInputStream(entry));
                publishProgress(4, 4, 3);
            }
            zipFile.close();
            inFile.delete();
            publishProgress(4, 4, 4);
            bHaveChanges = true;
        } else {
            String dbAlias = "incrDb";
            String pathToLocalArchive = FILES_PATH + DB_NAME_ZIP_TMP;
            String pathToLocalFile = DB_PATH + DB_NAME_TMP2;
            Archivator.unZipFile(pathToLocalArchive, pathToLocalFile);
            publishProgress(4, 4, 1);
            try {
                Db.getInstance().attathDb(pathToLocalFile, dbAlias);
                if (Db.getInstance().isOtherDbAttached()) {
                    publishProgress(4, 4, 2);
                    if ((this.recieveType & SYNC_TYPE_REST) > 0) {
                        mergeRests(dbAlias);
                    } else {
                        mergeDatabases(dbAlias);
                    }
                    publishProgress(4, 4, 3);
                    Db.getInstance().detachDb(dbAlias);
                }
            } catch (Exception ex) {
                ErrorHandler.CatchError("Exception in Synchronizer::ReceiveDatabase", ex);
            } finally {
                File f = new File(pathToLocalFile);
                f.delete();
                f = null;
                if (Db.getInstance().isOtherDbAttached()) Db.getInstance().detachDb(dbAlias);
            }
            publishProgress(4, 4, 4);
            bHaveChanges = isTransfer;
        }
    }

    private void recieveFiles(String serverUrl) throws Exception {
        publishProgress(4, 4, 0);
        bHaveChanges = false;
        FileOutputStream fileOutStream;
        try {
            fileOutStream = context.openFileOutput(FILES_NAME_ZIP, 0);
            Log.e("serverUrl", serverUrl);
            bHaveChanges = fileTransfer(null, false, null, fileOutStream, serverUrl);
            publishProgress(4, 4, 2);
            fileOutStream.flush();
            fileOutStream.close();

            if (bHaveChanges) {
                String pathToLocalArchive = FILES_PATH + FILES_NAME_ZIP;

                File sdFilePath = new File(SD_FILES_PATH);

                try {
                    sdFilePath.mkdirs();
                } catch (Exception ex) {
                }
                ;

                if (sdFilePath.exists()) {
                    Archivator.unZipAll(pathToLocalArchive, SD_FILES_PATH);
                    bHaveChanges = true;
                    publishProgress(4, 4, 3);
                } else {
                    syncError = "SD CARD NOT CONNECTED!"; //context.getResources().getString(R.string.sync_actual_version);
                    bHaveChanges = false;
                }
            }
        } catch (FileNotFoundException ex) {
            ErrorHandler.CatchError("Exception in Synchronizer::ReceiveDatabase", ex);
            bHaveChanges = false;
        }
        publishProgress(4, 4, 4);
    }

    //--------------------------------------------------------------    
    public void copyDataBase(InputStream inputStream) throws IOException {
        // Path to db
        String outFileName = DB_PATH + DB_NAME;
        copyDataBase(inputStream, outFileName);
    }

    public void copyDataBase(InputStream inputStream, String outFileName) throws IOException {
        //Open the empty db as the output stream
        OutputStream outputStream = new FileOutputStream(outFileName);

        copyFile(inputStream, outputStream);

        //Close the streams
        outputStream.flush();
        outputStream.close();
    }

    public static void copyFile(InputStream inputStream, OutputStream outputStream) throws IOException {
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
    }

    public static void backupDataBase() throws IOException {
        String SDCARD_PATH = "/sdcard/";
        String outFileName = SDCARD_PATH + DB_NAME;
        OutputStream outputStream = new FileOutputStream(outFileName);

        String inFileName = DB_PATH + DB_NAME;
        InputStream inputStream = new FileInputStream(inFileName);

        copyFile(inputStream, outputStream);

        try {
            inFileName = DB_PATH + DB_NAME_BAK;
            File bakup = new File(inFileName);
            if (bakup.exists()) {
                inputStream = new FileInputStream(inFileName);
                copyFile(inputStream, outputStream);
            }
        } catch (Exception ex) {
            ErrorHandler.CatchError("Exception in Synchronizer::backupDataBase", ex);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    private void mergeDatabases(String dbAlias) {
        String strSql = String.format("select SyncTableName from %1s.SyncTables", dbAlias);
        Cursor curTables = Db.getInstance().selectSQL(strSql);

        if (curTables.moveToFirst()) {
            do {
                try {
                    String tableName = curTables.getString(0);
                    strSql = String.format("replace into %1s select * from %2s.%3s", tableName, dbAlias, tableName);
                    Db.getInstance().execSQL(strSql);
                } catch (Exception ex) {
                    ErrorHandler.CatchError("Exception in Synchronizer::mergeDataBases", ex);
                }
            } while (curTables.moveToNext());
        }
    }

    private void markEntitiesSended() {
        String strSql = String.format("select SyncTableName from SyncTables where SyncTypeID = %1s", this.sendType);
        Cursor curTables = Db.getInstance().selectSQL(strSql);

        if (curTables.moveToFirst()) {
            do {
                try {
                    String tableName = curTables.getString(0);
                    strSql = String.format("update %1s set Sent = 1 where Sent = 0", tableName);
                    Db.getInstance().execSQL(strSql);
                } catch (Exception ex) {
                    ErrorHandler.CatchError("Exception in Synchronizer::markEntitiesSended", ex);
                }
            } while (curTables.moveToNext());

            strSql = String.format("update Documents set State = '" + Document.DOC_STATE_SENT + "' where Sent = 1 and State = '" + Document.DOC_STATE_FINISHED + "'");
            Db.getInstance().execSQL(strSql);
        }
    }

    private void mergeRests(String dbAlias) {
        try {
            String strSql = String.format("update Rest set Rest = coalesce((select Rest from %1s.Rest r where Rest.ItemID = r.ItemID), 0)", dbAlias);
            Db.getInstance().execSQL(strSql);

            strSql = String.format("update CurDocDetails set Quantity = coalesce((select Rest from %1s.Rest r where CurDocDetails.ItemID = r.ItemID and CurDocDetails.DocID = 0), 0)", dbAlias);
            Db.getInstance().execSQL(strSql);
        } catch (Exception ex) {
            ErrorHandler.CatchError("Exception in Synchronizer::mergeRests", ex);
        }

    }

    private void truncateEntitiesNotSend(String dbName) {
        Db dbToClear = new Db(AntContext.getInstance().getContext(), dbName);

        int totalCnt = 0;
        int totalDone = 0;

        if (this.sendType == SYNC_TYPE_INCR_SEND) {
            String strSql = "select st.SyncTableName " +
                    "from SyncTables st " +
                    "where st.SyncTypeID = " + this.sendType;

            Cursor curTablesIncr = dbToClear.selectSQL(strSql);

            totalCnt = curTablesIncr.getCount();
            totalDone = 0;

            if (curTablesIncr.moveToFirst()) {
                do {
                    try {
                        String tableName = curTablesIncr.getString(0);
                        if (!Convert.isNullOrBlank(tableName)) {
                            strSql = String.format("delete from %1s where Sent = 1", tableName);
                            dbToClear.execSQL(strSql);
                            publishProgress(1, totalCnt, totalDone++);
                        }
                    } catch (Exception ex) {
                        ErrorHandler.CatchError("Exception in Synchronizer::truncateEntitiesNotSend", ex);
                    }
                } while (curTablesIncr.moveToNext());
            }
        }

        String strSql = "select st.SyncTableName from SyncTables st " +
                "where st.SyncTypeID = " + SYNC_TYPE_FULL +
                " and not exists (select st2.SyncTableName from SyncTables st2 where st.SyncTableName = st2.SyncTableName and st2.SyncTypeID = " + this.sendType + ")";

        Cursor curTables = dbToClear.selectSQL(strSql);

        totalCnt = curTables.getCount();
        totalDone = -1;

        if (curTables.moveToFirst()) {
            do {
                try {
                    String tableName = curTables.getString(0);
                    if (!Convert.isNullOrBlank(tableName)) {
                        strSql = String.format("delete from %1s", tableName);
                        dbToClear.execSQL(strSql);
                        publishProgress(1, totalCnt, totalDone++);
                    }
                } catch (Exception ex) {
                    ErrorHandler.CatchError("Exception in Synchronizer::truncateEntitiesNotSend", ex);
                }

            } while (curTables.moveToNext());

            try {
                dbToClear.vacuum();
            } catch (Exception ex) {
                Log.e("vacuum", ex.toString());
            }
        }
    }

    public String receiveUpdateApk(String serverUrl) throws Exception {
        String result = "";
        HttpURLConnection connection = null;

        int serverResponseCode = HttpURLConnection.HTTP_BAD_REQUEST;
        String serverResponseMessage = "";

        try {
            URL url = new URL(serverUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();

            int contentLen = 0;
            int contentDone = 0;

            publishProgress(0, 0, 0);

            String PATH = Environment.getExternalStorageDirectory() + "/download/";
            File file = new File(PATH);
            file.mkdirs();
            File outputFile = new File(file, "ant_up_from_" + Api.getVersionName().replace(' ', '_') + ".apk");
            FileOutputStream fos = new FileOutputStream(outputFile);

            InputStream is = connection.getInputStream();
            contentLen = connection.getContentLength();

            if (connection != null) {
                serverResponseCode = connection.getResponseCode();
                serverResponseMessage = connection.getResponseMessage();
            }

            if (contentLen > 0) {
                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                    contentDone += len1;
                    publishProgress(2, contentLen, contentDone);
                }
                fos.close();
                is.close();

                publishProgress(2, 1, 1); //100%

                result = "file://" + outputFile.getAbsolutePath();
            } else {
                syncError = context.getResources().getString(R.string.sync_actual_version);
            }
        } catch (Exception ex) {
            throw new Exception(serverResponseMessage + ". " + ex); //Exception handling			
        } finally {
            publishProgress(3, 0, 0);
            if (connection != null) connection.disconnect();
        }

        return result;
    }

    private String getConnectParameters(Integer syncType) {
        return getConnectParameters(syncType, false);
    }

    private String getConnectParameters(Integer syncType, boolean withoutVersion) {
        String deviceId = "id=" + AntContext.getInstance().getDeviceId();
        String deviceKey = "key=" + AntContext.getInstance().getDeviceKey();
        String sync = "sync=" + syncType;
        String version = "ver=" + Api.getVersionName();

        String servletName = "";

        if ((syncType & (SYNC_TYPE_FULL_SEND | SYNC_TYPE_INCR_SEND)) > 0) {
            servletName = "upload";
        } else if ((syncType & (SYNC_TYPE_FULL | SYNC_TYPE_INCR | SYNC_TYPE_REST | SYNC_TYPE_SALDO)) > 0) {
            servletName = "recieve";
        } else if (syncType == SYNC_TYPE_UPDATE) {
            servletName = "update";
        } else if (syncType == SYNC_TYPE_MEDIA) {
            servletName = "syncmedia";
        } else if (syncType == SYNC_TYPE_MEDIA_SEND) {
            servletName = "uploadmediafile";
        }

        String s = "/" + servletName + "?" + deviceId + "&" + deviceKey + "&" + sync;
        if (withoutVersion)
            return s;
        else
            return s + "&" + version;
    }

    public boolean checkAndCreateMediaPath(String path) {
        boolean result = false;
        try {
            File f = new File(path);
            if (!(result = f.exists())) {
                result = f.mkdirs();
            }
        } catch (Exception ex) {
            ErrorHandler.CatchError("Syncronizer.checkAndCreateMediaPath", ex);
        }
        return result;
    }
}
