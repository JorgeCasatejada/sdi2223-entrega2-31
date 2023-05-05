module.exports = {
    mongoClient: null,
    app: null,
    init: function (app, mongoClient) {
        this.mongoClient = mongoClient;
        this.app = app;
    },
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
