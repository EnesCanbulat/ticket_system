
Ticket System
Spring Boot ve MySQL ile oluşturulmuş müşteri destek sistemi.




## Özellikler

Müşteri Yönetimi: Müşteri profilleri oluşturun, güncelleyin ve yönetin.
Temsilci/Temsilci Yönetimi: Destek temsilcilerini ve atamalarını yönetin.
Bildirim Sistemi: Destek talepleri oluşturun, atayın, takip edin ve çözün.
Mesajlaşma Sistemi: Müşteriler ve temsilciler arasında gerçek zamanlı iletişim.
Öncelik Yönetimi: Talepleri aciliyet seviyelerine göre yönetin.
Durum Takibi: Ticket oluşturmad aşamsaından çözüme kadar izleyin.
Gösterge Paneli: Atanmış ve atanmamış taleplerin bulunduğu temsilci panosu.

Java 17+ (JDK 24 compatible)
Spring Boot 3.5.3
MySQL 8.0+
Maven 3.6+
Postman (API Testleriniz için)
Lombok
  
## Database Kurulumu

### 1) Veritabanını ayarla
Varsayılan profil MySQL kullanır. `src/main/resources/application.properties` içinde:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/ticket_system}
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
management.endpoints.web.exposure.include=health,info,mappings
```



$env:DB_URL="jdbc:mysql://localhost:3306/ticket_system"


> **Not:** `application-dev.properties` dosyası, yerelde çalışırken tipik MySQL parametreleri (örn. `createDatabaseIfNotExist=true`, `allowPublicKeyRetrieval=true`, `useSSL=false`, `serverTimezone=UTC`) ile yapılandırılmıştır.

### 2) Derleyin ve çalıştırın.

# Derleme
mvn clean package

mvn spring-boot:run -Dspring-boot.run.profiles=dev

# veya jar üzerinden
java -jar target/ticket-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev


Uygulama varsayılan olarak `http://localhost:8080` üzerinde çalışır.

- Sağlık kontrolü: `GET /api/utility/health`
- Actuator Health: `GET /actuator/health`
- Ping: `GET /api/ping`
## Mimari
src/main/java/com/example/ticketsystem
├── controller/          # REST controller’lar
├── dto/                 # Request/Response DTO’ları
├── entity/              # JPA varlıkları
├── repository/          # Spring Data JPA repository’leri
├── service/             # Servis arabirimleri ve iş mantığı
└── TicketSystemApplication.java

src/main/resources
├── application.properties
├── application-dev.properties
├── db/migration/        # Flyway SQL migration’ları (V001...V007)
└── static/index.html    # Basit test sayfası

## Veritabanı Şeması

`src/main/resources/db/migration` altında:
- **V001** – `agents`
- **V002** – `customers`
- **V003** – `ticket_priorities`
- **V004** – `ticket_statuses`
- **V005** – `tickets`
- **V006** – `ticket_messages`
- **V007** – `wb_check`

# Flyway'i Maven ile Çalıştırma 
mvn -Dflyway.url=jdbc:mysql://localhost:3306/ticket_system     -Dflyway.user=root     -Dflyway.password=YOUR_PASSWORD     flyway:migrate
## Base Url
http://localhost:8080

## Health ve Ping Kontrolü
- `GET /api/ping` → `{ "pong": "ok" }`
- `GET /api/utility/health` → `{ "status": "UP", "time": "..." }`

## Müşteriler (`/api/customers`)
- `POST /api/customers` – **Müşteri oluştur**
  ```json
  {
    "name": "Enes Canbulat",
    "email": "enes@example.com",
    "phone": "+905551112233"
  }
  ```
- `GET /api/customers` – **Listele** (sayfalı)
- `GET /api/customers/{id}` – **Detay**
- `GET /api/customers/search?email=...` – **E-mail ile bul**
- `PUT /api/customers/{id}` – **Güncelle**
- `DELETE /api/customers/{id}` – **Sil**

## Ticket’lar (`/api/tickets`)
- `POST /api/tickets` – **Ticket oluştur**
  ```json
  {
    "customerId": 1,
    "title": "Siparişim teslim edilmedi",
    "description": "3 gündür kargo hareket etmiyor, yardım rica ederim.",
    "priorityId": 2
  }
  ```
- `GET /api/tickets` – **Listele** (sayfalı)
- `GET /api/tickets/{id}` – **Detay**
- `PATCH /api/tickets/{id}/status/{statusId}` – **Durum güncelle**
- `POST /api/tickets/{id}/assign` – **Temsilci ata**
  ```json
  { "agentId": 5, "note": "Yoğunluk nedeniyle bu ticket 5 no'lu temsilciye devredildi" }
  ```
- `POST /api/tickets/{id}/messages` – **Mesaj ekle**
  ```json
  { "senderId": 1, "message": "Merhaba, talebinizi aldık." }
  ```
- `GET /api/tickets/{id}/messages` – **Mesajları çek**
- `POST /api/tickets/{id}/close` – **Ticket kapat**


  ## Temsilciler (`/api/representatives`)
- `POST /api/representatives` – **Temsilci oluştur**
  ```json
  { "name": "Ayşe Yılmaz", "email": "ayse@company.com", "phone": "+902122223344" }
  ```
- `GET /api/representatives` – **Listele** (sayfalı)
- `GET /api/representatives/{id}` – **Detay**
- `PUT /api/representatives/{id}` – **Güncelle**
- `DELETE /api/representatives/{id}` – **Sil**
- `GET /api/representatives/{agentId}/tickets` – **Temsilciye atanmış ticket’lar**
- `GET /api/representatives/tickets/unassigned` – **Atanmamış ticket’lar**
- `POST /api/representatives/{agentId}/tickets/{ticketId}/assign` – **Ticket’ı temsilciye ata**
- `POST /api/representatives/{agentId}/tickets/{ticketId}/reply` – **Temsilci cevabı gönder**
  ```json
  {
    "agentId": 5,
    "message": "Talebiniz işleme alındı.",
    "newStatusId": 3,
    "isInternal": false
  }
  ```
- `PATCH /api/representatives/{agentId}/tickets/{ticketId}/status?statusId=3&note=...` – **Durum değiştir**
- `POST /api/representatives/{agentId}/tickets/{ticketId}/close` – **Kapat**
- `GET /api/representatives/{agentId}/dashboard` – **Özet panel** (atanmış/atanmamış sayıları ve son listeler)



## Yazarlar ve Teşekkür

 Enes Canbulat tarafından geliştirilmiştir.
