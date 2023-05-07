module.exports = {
    mongoClient: null,
    app: null,
    init: function (app, mongoClient) {
        this.mongoClient = mongoClient;
        this.app = app;
    },
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
