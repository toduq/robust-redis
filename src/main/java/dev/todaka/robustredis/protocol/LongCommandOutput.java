package dev.todaka.robustredis.protocol;

public class LongCommandOutput extends CommandOutput<Long> {
    @Override
    public void setResult(long result) {
        this.future.complete(result);
    }
}
