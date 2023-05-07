package com.uniovi.sdi2223entrega2test31;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;


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
        insertConversations();
        insertMessages();
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

    public long othersOffersSize(String email) {
        List<Document> results = getMongodb().getCollection("offers").find(ne("author", email)).into(new ArrayList<>());
        return results.size();
    }

    public String getOneOfferIdByAuthor(String email) {
        Document offer = getMongodb().getCollection("offers").find(eq("author", email)).first();
        if (offer != null) {
            ObjectId id = offer.getObjectId("_id");
            return id.toString();
        }
        return " ";
    }

    public String getOneConverIdByParticipants(String owner, String offertant) {
        Document conver = getMongodb().getCollection("convers").find(and(eq("owner", owner), eq("offertant", offertant))).first();
        if (conver != null) {
            ObjectId id = conver.getObjectId("_id");
            return id.toString();
        }
        return " ";
    }

    public String getOneMessageIdByAuthor(String email) {
        Document message = getMongodb().getCollection("messages").find(eq("author", email)).first();
        if (message != null) {
            ObjectId id = message.getObjectId("_id");
            return id.toString();
        }
        return " ";
    }

    private void deleteData() {
        getMongodb().getCollection("logs").drop();
        getMongodb().getCollection("offers").drop();
        getMongodb().getCollection("purchases").drop();
        getMongodb().getCollection("users").drop();
        getMongodb().getCollection("convers").drop();
        getMongodb().getCollection("messages").drop();
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

    private void insertConversations() {
        MongoCollection<Document> convers = getMongodb().getCollection("convers");
        MongoCursor<Document> it = getMongodb().getCollection("offers").find(eq("author", "user15@email.com")).iterator();
        for (int j = 1; j <= 10; j++){
            Document offer = it.next();
            Document conver = new Document()
                    .append("offertant", "user08@email.com")
                    .append("idOffer", offer.getObjectId("_id"))
                    .append("owner", "user15@email.com");

            convers.insertOne(conver);
        }

        it.close();

    }

    private void insertMessages() {
        MongoCollection<Document> messages = getMongodb().getCollection("messages");
        MongoCursor<Document> it = getMongodb().getCollection("convers").find().iterator();
        boolean bol = true;
        while(it.hasNext()) {
            Document conver = it.next();
            Document message = new Document()
                    .append("author", "user08@email.com")
                    .append("text", "hola")
                    .append("date", "7/5/23 0:45:54 UTC")
                    .append("read", bol)
                    .append("idConver", conver.getObjectId("_id"));

            messages.insertOne(message);
            bol = !bol;
        }

        it.close();

    }

}
