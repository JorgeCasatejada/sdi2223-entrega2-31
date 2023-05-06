module.exports = {
    mongoClient: null,
    app: null,
    init: function (app, mongoClient) {
        this.mongoClient = mongoClient;
        this.app = app;
    }, findUser: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'users';
            const usersCollection = database.collection(collectionName);
            const user = await usersCollection.findOne(filter, options);
            return user;
        } catch (error) {
            throw (error);
        }
    }, insertUser: async function (user) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'users';
            const usersCollection = database.collection(collectionName);
            const result = await usersCollection.insertOne(user);
            return result.insertedId;
        } catch (error) {
            throw (error);
        }
    }, getUsersPg: async function (filter, options, page) {
        try {
            const limit = 4;
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'users';
            const usersCollection = database.collection(collectionName);
            const usersCollectionCount = await usersCollection.count();
            const cursor = usersCollection.find(filter, options).skip((page - 1) * limit).limit(limit)
            const users = await cursor.toArray();
            const result = {users: users, total: usersCollectionCount};
            return result;
        } catch (error) {
            throw (error);
        }
    }, deleteUser: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionNameUsers = 'users';
            const usersCollection = database.collection(collectionNameUsers);
            const collectionNameOffers = 'offers';
            const offersCollection = database.collection(collectionNameOffers);
            await offersCollection.deleteMany({author: filter.email}, {});
            const collectionNameConversations = 'convers';
            const conversationsCollection = database.collection(collectionNameConversations);
            await conversationsCollection.deleteMany({owner: filter.email}, {});
            const collectionNameMessages = 'messages';
            const messagesCollection = database.collection(collectionNameMessages);
            await messagesCollection.deleteMany({author: filter.email}, {});
            const result = await usersCollection.deleteOne(filter, options);
            return result;
        } catch (error) {
            throw (error);
        }
    }, deleteUsers: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionNameUsers = 'users';
            const usersCollection = database.collection(collectionNameUsers);
            const collectionNameOffers = 'offers';
            const offersCollection = database.collection(collectionNameOffers);
            await offersCollection.deleteMany({author: filter.email}, {});
            const collectionNameConversations = 'convers';
            const conversationsCollection = database.collection(collectionNameConversations);
            await conversationsCollection.deleteMany({owner: filter.email}, {});
            const collectionNameMessages = 'messages';
            const messagesCollection = database.collection(collectionNameMessages);
            await messagesCollection.deleteMany({author: filter.email}, {});
            const result = await usersCollection.deleteMany(filter, options);
            return result;
        } catch (error) {
            throw (error);
        }
    }, decrementWallet: async function (email, amount) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'users';
            const usersCollection = database.collection(collectionName);
            // restarle a la cartera la cantidad especificada
            const result = await usersCollection.updateOne(
                { email: email },
                { $inc: { wallet: -amount } }
            );
            return result;
        } catch (error) {
            throw error;
        }
    }, dropDatabase: async function () {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            await database.dropDatabase();
        } catch (error) {
            throw error;
        }
    }
};
