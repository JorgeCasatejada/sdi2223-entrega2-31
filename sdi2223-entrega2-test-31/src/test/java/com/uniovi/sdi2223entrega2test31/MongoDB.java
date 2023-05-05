package com.uniovi.sdi2223entrega2test31;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

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

    private void deleteData() {
        getMongodb().getCollection("logs").drop();
        getMongodb().getCollection("offers").drop();
        getMongodb().getCollection("purchases").drop();
        getMongodb().getCollection("users").drop();
    }

    private void insertUsuarios() {
        MongoCollection<Document> usuarios = getMongodb().getCollection("usuarios");
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

    }

}
