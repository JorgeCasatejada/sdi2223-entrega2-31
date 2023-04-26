let createError = require('http-errors');
let express = require('express');
let path = require('path');
let cookieParser = require('cookie-parser');
let logger = require('morgan');

let app = express();

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

const userSessionRouter = require('./routes/userSessionRouter');
app.use("/admin/users",userSessionRouter);
app.use("/offer/add",userSessionRouter);
app.use("/user/offers",userSessionRouter);

let crypto = require('crypto');
app.set('clave','abcdefg');
app.set('crypto',crypto);

let bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));


const usersRepository = require("./repositories/usersRepository.js");
usersRepository.init(app, MongoClient);

const offersRepository = require("./repositories/offersRepository.js");
offersRepository.init(app, MongoClient);

require("./routes/users.js")(app, usersRepository, offersRepository);
require("./routes/admin.js")(app, usersRepository);
require("./routes/offers.js")(app, offersRepository);

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'twig');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

let datosIns = false;
app.use('/', (req, res, next) => {
  if (!datosIns) {
    datosIns = true;
    addToDB();
  }
  next();
}, indexRouter);

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
        author: "user" + i + "@email.com"
      }
      offersRepository.insertOffer(offer);
    }
  }
}

module.exports = app;
