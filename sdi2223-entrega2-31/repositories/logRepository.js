module.exports = {
    mongoClient: null,
    app: null,
    logger: null,
    init: function (app, mongoClient, logger) {
        this.mongoClient = mongoClient;
        this.app = app;
        this.logger = logger;
    }, insertLog: async function (type, text) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db('myWallapop');
            const collection = database.collection('logs');
            // meter tipo, mensaje y fecha-hora en timestamp
            const result = await collection.insertOne({
                type,
                text,
                timestamp: Date.now()
            });
            this.logger.debug(`Log inserted with id ${result.insertedId}`);
        } catch (error) {
            this.logger.error('Error inserting log', error);
        }
    }, getLogs: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'logs';
            const logsCollection = database.collection(collectionName);
            const logs = await logsCollection.find(filter, options).toArray();
            logs.forEach(log => {
                log.readableDate = new Date(log.timestamp).toLocaleString();
            });
            return logs;
        } catch (error) {
            throw (error);
        }
    }, deleteLogs: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'logs';
            const logsCollection = database.collection(collectionName);
            const logs = await logsCollection.deleteMany(filter, options);
            return logs;
        } catch (error) {
            throw (error);
        }
    }
};
