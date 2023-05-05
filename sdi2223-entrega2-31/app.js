let createError = require('http-errors');
let express = require('express');
let path = require('path');
let cookieParser = require('cookie-parser');
let logger = require('morgan');

let app = express();

let jwt = require('jsonwebtoken');
app.set('jwt',jwt);

let indexRouter = require('./routes/index');

let expressSession = require('express-session');
app.use(expressSession({
  secret: 'abcdefg',
  resave: true,
  saveUninitialized: true
}));

const { MongoClient } = require("mongodb");
const url = "mongodb://localhost:27017";
app.set('connectionStrings', url);

const adminSessionRouter = require('./routes/adminSessionRouter');
app.use("/admin",adminSessionRouter);
const userSessionRouter = require('./routes/userSessionRouter');
app.use("/offer/add",userSessionRouter);
app.use("/offer/delete",userSessionRouter);
app.use("/offer/highlight",userSessionRouter);
app.use("/user/offers",userSessionRouter);
app.use("/user/logout",userSessionRouter);
app.use("/offers",userSessionRouter);
app.use("/offers/buy", userSessionRouter);
app.use("/offers/purchases", userSessionRouter);

let crypto = require('crypto');
app.set('clave','abcdefg');
app.set('crypto',crypto);

const log4js = require('log4js');
log4js.configure(
    {
      appenders: {
        console: { type: 'console' },
        file: { type: 'file', filename: 'logs/log.log' }
      },
      categories: {
        default: { appenders: ['console', 'file'], level: 'info' }
      }
    }
);

let bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

const userTokenRouter = require('./routes/userTokenRouter');
app.use("/api/v1.0/offers/", userTokenRouter);
app.use("/api/v1.0/convers/", userTokenRouter);
app.use("/api/v1.0/messages/", userTokenRouter);

const usersRepository = require("./repositories/usersRepository.js");
usersRepository.init(app, MongoClient);

const offersRepository = require("./repositories/offersRepository.js");
offersRepository.init(app, MongoClient);

const logRepository = require("./repositories/logRepository.js");
logRepository.init(app, MongoClient, log4js.getLogger("logRepository"));

const conversRepository = require("./repositories/conversRepository.js");
conversRepository.init(app, MongoClient);

const messagesRepository = require("./repositories/messagesRepository.js");
messagesRepository.init(app, MongoClient);

require("./routes/users.js")(app, usersRepository, offersRepository, logRepository, log4js.getLogger("users"));
require("./routes/admin.js")(app, usersRepository, logRepository, log4js.getLogger("admin"));
require("./routes/offers.js")(app, usersRepository, offersRepository, logRepository, log4js.getLogger("offers"));
require("./routes/api/APIv1.0.js")(app, usersRepository, offersRepository, conversRepository, messagesRepository, logRepository, log4js.getLogger("api"));

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'twig');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

addToDB();

app.use('/', indexRouter);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

function addToDB() {

  let securePassword = app.get("crypto").createHmac('sha256', app.get('clave'))
      .update("admin").digest('hex');
  let admin = {
    email: "admin@email.com",
    name: "admin",
    surname: "admin",
    birthDate: "26/12/1991",
    password: securePassword,
    wallet: 100,
    profile: "Usuario Administrador"
  }

  let filter = {
    email: admin.email
  }
  usersRepository.findUser(filter, {}).then(user => {
    if (user == null) {
      usersRepository.insertUser(admin);
      for (let i = 1; i <= 20; i++) {
        if (i < 10) {
          i = "0" + i;
        }
        let securePassword = app.get("crypto").createHmac('sha256', app.get('clave'))
            .update("user" + i).digest('hex');
        let user = {
          email: "user" + i + "@email.com",
          name: "user" + i,
          surname: "user" + i,
          birthDate: i + "/12/1991",
          password: securePassword,
          wallet: 100,
          profile: "Usuario Estándar"
        }
        usersRepository.insertUser(user);
      }
      createOffers();
    }
  }).catch(error => {
    req.session.user = null;
    res.redirect("/users/login" +
        "?message=Se ha producido un error al buscar el usuario" +
        "&messageType=alert-danger ");
  });
}

function createOffers() {
  for (let i = 1; i <= 20; i++) {
    if (i < 10) {
      i = "0" + i;
    }
    for (let j = 1; j <= 10; j++){
      let offer = {
        title: "Ejemplo " + j*i,
        description: "Descripción ejemplo " + j*i,
        date: new Date().toLocaleDateString('es-ES'),
        price: j*3,
        author: "user" + i + "@email.com",
        sold: false,
        highlighted: false
      }
      offersRepository.insertOffer(offer);
    }
  }
}

module.exports = app;
