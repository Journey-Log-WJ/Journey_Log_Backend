package com.wonjun.journeylog.service.sync;

public record PostSyncResult(int created, int updated, int skipped, int deleted) {
}
