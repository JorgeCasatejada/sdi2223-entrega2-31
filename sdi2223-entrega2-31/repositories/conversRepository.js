module.exports = {
    mongoClient: null,
    app: null,
    init: function (app, mongoClient) {
        this.mongoClient = mongoClient;
        this.app = app;
    },
    // Método para insertar una conversación en la BBDD
    insertConver: async function (conver) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'convers';
            const conversCollection = database.collection(collectionName);
            const result = await conversCollection.insertOne(conver);
            return result.insertedId;
        } catch (error) {
            throw (error);
        }
    },
    // Método para encontrar una conversación de la BBDD
    findConver: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'convers';
            const conversCollection = database.collection(collectionName);
            const conver = await conversCollection.findOne(filter, options);
            return conver;
        } catch (error) {
            throw (error);
        }
    },
    // Obtener un listado de conversaciones
    getConvers: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'convers';
            const conversCollection = database.collection(collectionName);
            const convers = await conversCollection.find(filter, options).toArray();
            return convers;
        } catch (error) {
            throw (error);
        }
    },
    // Eliminar una conversación de la base de datos
    deleteConver: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'convers';
            const conversCollection = database.collection(collectionName);
            const result = await conversCollection.deleteOne(filter, options);
            return result;
        } catch (error) {
            throw (error);
        }
    }
};
