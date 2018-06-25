package ru.sbt.integration.orchestration.mapper.utils;

import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;


public enum Notifications {
    ERROR_NOTIFICATION(Position.BOTTOM_CENTER, Notification.Type.ERROR_MESSAGE) {
        @Override
        public void show(String caption, String description) {
            Notification notification = new Notification(caption, description, this.type);
            notification.setPosition(this.position);
            notification.show(Page.getCurrent());
        }
    },
    ACTION_NOTIFICATION(Position.MIDDLE_CENTER, Notification.Type.HUMANIZED_MESSAGE) {
        @Override
        public void show(String caption, String description) {
            Notification notification = new Notification(caption, description, this.type);
            notification.setPosition(this.position);
            notification.setDelayMsec(2_000);
            notification.show(Page.getCurrent());
        }
    };

    final Position position;
    final Notification.Type type;

    Notifications(Position position, Notification.Type type) {
        this.position = position;
        this.type = type;
    }

    public void show(String caption) {
        this.show(caption, null);
    }

    public abstract void show(String caption, String description);
}
