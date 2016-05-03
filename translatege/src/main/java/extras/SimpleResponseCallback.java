package extras;

public interface SimpleResponseCallback {
    void onSuccess(String... params);

    void onError(String... params);
}
