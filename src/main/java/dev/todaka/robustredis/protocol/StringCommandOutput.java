package dev.todaka.robustredis.protocol;

public class StringCommandOutput extends CommandOutput<String> {
    @Override
    public void setResult(String result) {
        this.future.complete(result);
    }
}
