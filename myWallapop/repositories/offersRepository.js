module.exports = {
    mongoClient: null,
    app: null,
    init: function (app, mongoClient) {
        this.mongoClient = mongoClient;
        this.app = app;
    }, insertOffer: async function (offer) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'offers';
            const offersCollection = database.collection(collectionName);
            const result = await offersCollection.insertOne(offer);
            return result.insertedId;
        } catch (error) {
            throw (error);
        }
    }, getOffersPg: async function (filter, options, page) {
        try {
            const limit = 4;
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'offers';
            const offersCollection = database.collection(collectionName);
            const collectionFiltered = await offersCollection.find(filter, options);
            const offersCollectionCount = await collectionFiltered.count();
            const cursor = collectionFiltered.skip((page - 1) * limit).limit(limit);
            const offers = await cursor.toArray();
            const result = {offers: offers, total: offersCollectionCount};
            return result;
        } catch (error) {
            throw (error);
        }
    }, deleteOffer: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'offers';
            const offersCollection = database.collection(collectionName);
            const result = await offersCollection.deleteOne(filter, options);
            return result;
        } catch (error) {
            throw (error);
        }
    }, findOffer: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'offers';
            const offersCollection = database.collection(collectionName);
            const offer = await offersCollection.findOne(filter, options);
            return offer;
        } catch (error) {
            throw (error);
        }
    }
};
