module com.oliinyk.costumes.carnivalrentalapp {
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;
    requires static lombok;
    requires transitive java.sql;
    requires com.zaxxer.hikari;
    requires flyway.core;
    requires jbcrypt;
    requires atlantafx.base;
    // Ikonli: базова бібліотека + FontAwesome 5 набір іконок
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.datatype.jsr310;
    
    requires jakarta.mail;
    
    requires com.google.zxing;
    requires com.google.zxing.javase;
    
    requires kernel;
    requires layout;
    requires io;

    exports com.oliinyk.costumes.model;
    exports com.oliinyk.costumes.repository;
    exports com.oliinyk.costumes.service;
    exports com.oliinyk.costumes.infrastructure;
    
    // Експорт та відкриття пакетів для JavaFX
    exports com.oliinyk.costumes.view;
    opens com.oliinyk.costumes.view to javafx.fxml;
    exports com.oliinyk.costumes.viewmodel;
    
    // FlywayMarker.java робить db.migration реальним Java-пакетом.
    // Відкриваємо його для Flyway як fallback для classpath-сканування (запуск із JAR).
    opens db.migration to flyway.core;

    exports com.oliinyk.costumes;
}