module.exports = {
    mongoClient: null,
    app: null,
    init: function (app, mongoClient) {
        this.mongoClient = mongoClient;
        this.app = app;
    },
    // Método para insertar un mensaje
    insertMessage: async function (message) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'messages';
            const messagesCollection = database.collection(collectionName);
            const result = await messagesCollection.insertOne(message);
            return result.insertedId;
        } catch (error) {
            throw (error);
        }
    },
    // Método para encontrar un mensaje de la BBDD
    findMessage: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'messages';
            const messagesCollection = database.collection(collectionName);
            const message = await messagesCollection.findOne(filter, options);
            return message;
        } catch (error) {
            throw (error);
        }
    },
    // Método para extraer una lista de mensajes
    findMessages: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'messages';
            const messagesCollection = database.collection(collectionName);
            const messages = await messagesCollection.find(filter, options).toArray();
            return messages;
        } catch (error) {
            throw (error);
        }
    },
    // Método para el borrado de mensajes de la BBDD
    deleteMessages: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'messages';
            const messagesCollection = database.collection(collectionName);
            const result = await messagesCollection.deleteMany(filter, options);
            return result;
        } catch (error) {
            throw (error);
        }
    },
    // Método para actualizar un mensaje (se empleará para marcar a leídos)
    updateMessage: async function (newMessage, filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'messages';
            const messagesCollection = database.collection(collectionName);
            const result = await messagesCollection.updateOne(filter, {$set: newMessage}, options);
            return result;
        } catch (error) {
            throw (error);
        }
    },
    // Método para obtener mensajes con el campo "read" a false
    getUnreadMessages: async function (converId, autor) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'messages';
            const messagesCollection = database.collection(collectionName);
            // Contar mensajes no leidos en la conversacion
            const unreadMessages = await messagesCollection.find({
                idConver: converId,
                read: false,
                author: { $ne: autor }
            }).toArray();
            // Retornar la cantidad de mensajes no leidos que tiene la conversacion
            return unreadMessages.length;
        } catch (error) {
            throw (error);
        }
    }

};
