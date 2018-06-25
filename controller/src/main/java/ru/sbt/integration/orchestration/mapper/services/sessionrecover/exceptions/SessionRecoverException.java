package ru.sbt.integration.orchestration.mapper.services.sessionrecover.exceptions;

public class SessionRecoverException extends RuntimeException {
    public SessionRecoverException(String s) {
        super(s);
    }

    public SessionRecoverException(Throwable throwable) {
        this("внутренняя ошибка при восстановлении");
        throwable.printStackTrace();
    }
}
