package com.uniovi.sdi2223entrega2test31;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import static com.mongodb.client.model.Filters.eq;



public class MongoDB {

    private MongoClient mongoClient;
    private MongoDatabase mongodb;

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void setMongodb(MongoDatabase mongodb) {
        this.mongodb = mongodb;
    }

    public MongoDatabase getMongodb() {
        return mongodb;
    }

    public void resetMongo() {
        try {
            setMongoClient(new MongoClient(new MongoClientURI(
                    "mongodb://localhost:27017")));
                    setMongodb(getMongoClient().getDatabase("myWallapop"));
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        deleteData();
        insertUsuarios();
        insertOffers();
    }

    public List<String> usersEmail() {
        List<String> emails = new ArrayList<>();
        // Definir los campos a incluir o excluir
        Document projection = new Document("email", 1).append("_id", 0);

        MongoCollection<Document> usuarios =  getMongodb().getCollection("users");

        try (MongoCursor<Document> cursor = usuarios.find().projection(projection).iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                String email = document.getString("email");
                emails.add(email);
            }
        }
        return emails;
    }

    public List<String> getOffersByUser(String email) {
        List<String> offers = new ArrayList<>();
        // Definir los campos a incluir o excluir
        Document projection = new Document("author", 1).append("_id", 0);

        MongoCollection<Document> ofertas =  getMongodb().getCollection("offers");

        try (MongoCursor<Document> cursor = ofertas.find(eq("author", email)).projection(projection).iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                offers.add(document.toJson());
            }
        }
        return offers;
    }


    public long usersSize() {
        return getMongodb().getCollection("users").count();
    }

    public long offersSize() {
        return getMongodb().getCollection("offers").count();
    }

    private void deleteData() {
        getMongodb().getCollection("logs").drop();
        getMongodb().getCollection("offers").drop();
        getMongodb().getCollection("purchases").drop();
        getMongodb().getCollection("users").drop();
    }

    private void insertUsuarios() {
        MongoCollection<Document> usuarios = getMongodb().getCollection("users");
        Document admin = new Document().append("nombre", "admin").append("apellidos", "admin")
                .append("email", "admin@email.com")
                .append("password", "ebd5359e500475700c6cc3dd4af89cfd0569aa31724a1bf10ed1e3019dcfdb11")
                .append("wallet", 100).append("profile", "Usuario Administrador")
                .append("birthDate", "26/12/1991");
        usuarios.insertOne(admin);

        String number = "";
        for (int i = 1; i <= 20; i++) {
            if (i < 10) {
                number = "0" + i;
            } else {
                number = i + "";
            }
            Document user = new Document()
                    .append("name", "user" + number)
                    .append("surname", "user" + number)
                    .append("email", "user" + number + "@email.com")
                    .append("password", "ebd5359e500475700c6cc3dd4af89cfd0569aa31724a1bf10ed1e3019dcfdb11")
                    .append("wallet", 100)
                    .append("profile", "Usuario Estándar")
                    .append("birthDate", number + "/12/1991");
            usuarios.insertOne(user);
        }
    }

    private void insertOffers() {
        MongoCollection<Document> offers = getMongodb().getCollection("offers");
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = formatter.format(date);
        String number = "";
        for (int i = 1; i <= 20; i++) {
            if (i < 10) {
                number = "0" + i;
            } else {
                number = i + "";
            }
            for (int j = 1; j <= 10; j++){
                Document offer = new Document()
                        .append("title", "Ejemplo " + j*i)
                        .append("description", "Descripción ejemplo " + j*i)
                        .append("date", formattedDate)
                        .append("price", j*3)
                        .append("author", "user" + number + "@email.com")
                        .append("sold", false)
                        .append("highlighted", false);
                offers.insertOne(offer);
            }
        }
    }

}
