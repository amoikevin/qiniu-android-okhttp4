package androidx.qiniu;

import com.qiniu.android.common.AutoZone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.Configuration.Builder;
import com.qiniu.android.storage.KeyGenerator;
import com.qiniu.android.storage.Recorder;
import com.qiniu.android.storage.UpCancellationSignal;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.qiniu.android.storage.persistent.FileRecorder;

import java.io.File;
import java.util.concurrent.Callable;

import androidx.Action;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.content.ContextUtils;
import androidx.io.FileUtils;
import androidx.util.StringUtils;

/**
 * Created by JKL on 2017/12/1.
 */

public final class StorageUtils {
    private static UploadManager uploadManager;

    static {
        final Configuration config = defaults(new Builder()).build();
        uploadManager = new UploadManager(config);
    }

    public static boolean config(@NonNull final Action<Builder> callback) {
        final Builder builder = new Builder();

        try {
            callback.call(builder);
            uploadManager = new UploadManager(builder.build());
            return true;
        } catch (final Exception e) {
            e.printStackTrace();

        }
        return false;
    }

    @NonNull
    public static Builder defaults(@NonNull final Builder builder) {
        Recorder recorder = null;
        KeyGenerator keyGen = null;
        try {
            recorder = new FileRecorder(ContextUtils.getCachePath());
            keyGen = (key, file) -> StringUtils.format("%s_%s", FileUtils.getMd5HexString(file, 1024, true));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return builder.chunkSize(512 * 1024)        // 分片上传时，每片的大小。 默认256K
                .putThreshhold(1024 * 1024)   // 启用分片上传阀值。默认512K
                .connectTimeout(10)           // 链接超时。默认10秒
                .useHttps(true)               // 是否使用https上传域名
                .responseTimeout(60)          // 服务器响应超时。默认60秒
                .recorder(recorder)           // recorder分片上传时，已上传片记录器。默认null
                .recorder(recorder, keyGen)   // keyGen 分片上传时，生成标识符，用于片记录器区分是那个文件的上传记录
                .zone(AutoZone.autoZone)        // 设置区域，指定不同区域的上传域名、备用域名、备用IP。
                .dns(null);
    }

    @NonNull
    public static UpCancellationSignal toUpCancellationSignal(@Nullable final Callable<Boolean> cancel) {
        return () -> {
            if (cancel == null) return false;
            try {
                return cancel.call();
            } catch (final Exception e) {
                e.printStackTrace();
                return true;
            }
        };
    }

    @NonNull
    public static UpProgressHandler toUpProgressHandler(@Nullable final Action<Double> progress) {
        return (name, percent) -> {
            if (progress == null) return;
            try {
                progress.call(percent);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        };
    }

    public static void upload(@NonNull final String token, @NonNull final String key, @NonNull final String filePath, @NonNull final UploadOptions options) {
        uploadManager.syncPut(filePath, key, token, options);
    }

    public static void upload(@NonNull final String token, @NonNull final String key, @NonNull final byte[] data, @NonNull final UploadOptions options) {
        uploadManager.syncPut(data, key, token, options);
    }

    public static void upload(@NonNull final String token, @NonNull final String key, @NonNull final File file, @NonNull final UploadOptions options) {
        uploadManager.syncPut(file, key, token, options);
    }

    public static void upload(@NonNull final String token, @NonNull final String key, @NonNull final File file, @NonNull final UpCompletionHandler complete, @NonNull final UploadOptions options) {
        uploadManager.put(file, key, token, complete, options);
    }

    public static void upload(@NonNull final String token, @NonNull final String key, @NonNull final String filePath, @NonNull final UpCompletionHandler complete, @NonNull final UploadOptions options) {
        uploadManager.put(filePath, key, token, complete, options);
    }

    public static void upload(@NonNull final String token, @NonNull final String key, @NonNull final byte[] data, @NonNull final UpCompletionHandler complete, @NonNull final UploadOptions options) {
        uploadManager.put(data, key, token, complete, options);
    }

    public static void upload(@NonNull final String token, @NonNull final String key, @NonNull final String file, @NonNull final UpCompletionHandler complete, @Nullable final Action<Double> progress, @Nullable final Callable<Boolean> cancel) {
        uploadManager.put(file, key, token, complete, new UploadOptions(null, null, false, toUpProgressHandler(progress), toUpCancellationSignal(cancel)));
    }

    public static void upload(@NonNull final String token, @NonNull final String key, @NonNull final File file, @NonNull final UpCompletionHandler complete, @Nullable final Action<Double> progress, @Nullable final Callable<Boolean> cancel) {
        uploadManager.put(file, key, token, complete, new UploadOptions(null, null, false, toUpProgressHandler(progress), toUpCancellationSignal(cancel)));
    }

    public static void upload(@NonNull final String token, @NonNull final String key, @NonNull final byte[] data, @NonNull final UpCompletionHandler complete, @Nullable final Action<Double> progress, @Nullable final Callable<Boolean> cancel) {
        uploadManager.put(data, key, token, complete, new UploadOptions(null, null, false, toUpProgressHandler(progress), toUpCancellationSignal(cancel)));
    }

    @NonNull
    public static ResponseInfo upload(@NonNull final String token, @NonNull final String key, @NonNull final String file, @Nullable final Action<Double> progress, @Nullable final Callable<Boolean> cancel) {
        return uploadManager.syncPut(file, key, token, new UploadOptions(null, null, false, toUpProgressHandler(progress), toUpCancellationSignal(cancel)));
    }

    @NonNull
    public static ResponseInfo upload(@NonNull final String token, @NonNull final String key, @NonNull final File file, @Nullable final Action<Double> progress, @Nullable final Callable<Boolean> cancel) {
        return uploadManager.syncPut(file, key, token, new UploadOptions(null, null, false, toUpProgressHandler(progress), toUpCancellationSignal(cancel)));
    }

    @NonNull
    public static ResponseInfo upload(@NonNull final String token, @NonNull final String key, @NonNull final byte[] data, @Nullable final Action<Double> progress, @Nullable final Callable<Boolean> cancel) {
        return uploadManager.syncPut(data, key, token, new UploadOptions(null, null, false, toUpProgressHandler(progress), toUpCancellationSignal(cancel)));
    }
}
