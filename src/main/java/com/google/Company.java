package com.google;

import jakarta.persistence.*;
import java.util.UUID;

@Entity // Говорим Hibernate, что это таблица в базе данных
@Table(name = "companies") // Явно указываем имя таблицы
public class Company {

    @Id // Первичный ключ
    @GeneratedValue(strategy = GenerationType.UUID) // Пусть база сама генерирует UUID
    private UUID id;

    @Column(nullable = false) // Поле name не может быть пустым
    private String name;

    @Column(name = "owner_id")
    private UUID ownerId;

    // Hibernate требует пустой конструктор по умолчанию
    public Company() {}

    // Конструктор для удобного создания
    public Company(String name, UUID ownerId) {
        this.name = name;
        this.ownerId = ownerId;
    }

    // Дальше нужны Геттеры и Сеттеры (можно сгенерировать в VS Code или использовать Lombok позже)
    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
}