const {userSignUpValidator, userLoginValidator} = require("./validators/userValidator");
const {validationResult} = require("express-validator");

module.exports = function (app, usersRepository, offersRepository, logRepository, logger) {
  app.get('/myWallapop', async function (req, res) {
    // ----- LOG -------
    const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
    logger.info(logText);
    await logRepository.insertLog('PET', logText);
    // -----------------
    res.render("index.twig");
  });

  app.get('/users/signup', async function (req, res) {
    // ----- LOG -------
    const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
    logger.info(logText);
    await logRepository.insertLog('PET', logText);
    // -----------------
    res.render("signup.twig", {user: req.session.user});
  });

  app.post('/users/signup', userSignUpValidator, async function (req, res) {
    // -------- LOG ------------
    const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
    logger.info(logText);
    await logRepository.insertLog('PET', logText);
    // ---------------------
    try {
      //Validación en el servidor
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        let responseFail = "/users/signup?message=";
        errors.array().forEach(error => responseFail += error.msg + "<br>")
        res.redirect(responseFail + "&messageType=alert-danger");
      } else {
        //Cifrar contraseña
        let securePassword = app.get("crypto").createHmac('sha256', app.get('clave'))
            .update(req.body.password).digest('hex');

        //Crear usuario
        let date = new Date(req.body.date);
        let formattedDate = date.toLocaleDateString('es-ES');

        let user = {
          email: req.body.email,
          name: req.body.name,
          surname: req.body.surname,
          birthDate: formattedDate,
          password: securePassword,
          wallet: 100,
          profile: "Usuario Estándar"
        }

        //Añadir usuario
        usersRepository.insertUser(user).then(userId => {
          req.session.user = user.email;
          getWallet(req.session.user).then(async wallet => {
            // -------- LOG ------------
            const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
            logger.info(logText);
            await logRepository.insertLog('ALTA', logText);
            // -----------------
            res.render("users/userOffers.twig", { user: req.session.user, wallet: wallet });
          }).catch(error => {
            res.send("Se ha producido un error al obtener el monedero " + error)
          })
        }).catch(error => {
          req.session.user = null;
          res.redirect("/users/signup" +
              "?message=Se ha producido un error al registrar el usuario" +
              "&messageType=alert-danger");
        });

      }
    } catch (e) {
      req.session.user = null;
      res.redirect("/users/signup" +
          "?message=Se ha producido un error al comprobar las credenciales" +
          "&messageType=alert-danger ");
    }
  });


  app.get('/users/login', async function (req, res) {
    // ----- LOG -------
    const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
    logger.info(logText);
    await logRepository.insertLog('PET', logText);
    // -----------------
    res.render("login.twig", {user: req.session.user});
  });


  app.post('/users/login', userLoginValidator, function (req, res) {
    try {
      //Validación en el servidor
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        let responseFail = "/users/login?message=";
        errors.array().forEach(error => responseFail += error.msg + "<br>")
        res.redirect(responseFail + "&messageType=alert-danger");
      } else {
        //Recuperar contraseña
        let securePassword = app.get("crypto").createHmac('sha256', app.get('clave'))
            .update(req.body.password).digest('hex');
        let filter = {
          email: req.body.email,
          password: securePassword
        }
        let options = {};
        //Buscar usuario
        usersRepository.findUser(filter, options).then(async user => {
          if (user == null) {
            // -------- LOG ------------
            const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
            logger.info(logText);
            await logRepository.insertLog('LOGIN-ERR', req.body.email);
            // -----------------
            req.session.user = null;
            res.redirect("/users/login" +
                "?message=Email o password incorrecto" +
                "&messageType=alert-danger ");
          } else {
            req.session.user = user.email;
            if (user.profile === "Usuario Estándar"){
              res.redirect("/user/offers" +
                  "?message=Inicio de sesión correcto" +
                  "&messageType=alert-info ");
              // -------- LOG ------------
              const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
              logger.info(logText);
              await logRepository.insertLog('LOGIN-EX', req.session.user);
              // -----------------
            }
            else{
              res.redirect("/admin/users" +
                  "?message=Inicio de sesión correcto (Administrador)" +
                  "&messageType=alert-info ");
              // -------- LOG ------------
              const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
              logger.info(logText);
              await logRepository.insertLog('LOGIN-EX', req.session.user);
              // -----------------
            }
          }
        }).catch(async error => {
          // -------- LOG ------------
          const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
          logger.info(logText);
          await logRepository.insertLog('LOGIN-ERR', req.body.email);
          // -----------------
          req.session.user = null;
          res.redirect("/users/login" +
              "?message=Se ha producido un error al buscar el usuario" +
              "&messageType=alert-danger ");
        });
      }
    } catch (e) {
      req.session.user = null;
      res.redirect("/users/login" +
          "?message=Se ha producido un error al comprobar las credenciales" +
          "&messageType=alert-danger ");
    }
  });


  app.get('/users/logout', async function (req, res) {
    const prevUser = req.session.user != null ? true : false;
    // -------- LOG ------------
    const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
    logger.info(logText);
    await logRepository.insertLog('LOGOUT', req.session.user);
    // -----------------

    req.session.user = null;
    // si habia un usuario logueado, imprimir el mensaje, si no no (acceder a traves de url)
    if (prevUser){
      res.redirect("/users/login" +
          "?message=El usuario se ha desconectado correctamente" +
          "&messageType=alert-info ");
    }
    else{
      res.redirect("/users/login")
    }
  });


  app.get('/user/offers', async function (req, res) {
    // ----- LOG -------
    const logText = `[${new Date()}] - Mapping: ${req.originalUrl} - Método HTTP: ${req.method} -  
                  Parámetros ruta: ${JSON.stringify(req.params)} Parámetros consulta: ${JSON.stringify(req.query)}`;
    logger.info(logText);
    await logRepository.insertLog('PET', logText);
    // -----------------
    let filter = {author: req.session.user};
    let options = {sort: {highlighted: -1, title: 1}};

    let page = parseInt(req.query.page); // Es String !!!
    if (typeof req.query.page === "undefined" || req.query.page === null || req.query.page === "0") {
      page = 1;
    }
    offersRepository.getOffersPg(filter, options, page, 4).then(result => {
      let lastPage = result.total / 4;
      if (result.total % 4 > 0) { // Sobran decimales
        lastPage = lastPage + 1;
      }
      let pages = []; // paginas mostrar
      for (let i = page - 2; i <= page + 2; i++) {
        if (i > 0 && i <= lastPage) {
          pages.push(i);
        }
      }
      let response = {
        offers: result.offers,
        pages: pages,
        currentPage: page
      }
      getWallet(req.session.user).then(wallet => {
        res.render("users/userOffers.twig", {user: req.session.user, response: response, wallet: wallet});
      }).catch(error => {
        res.send("Se ha producido un error al obtener el monedero " + error)
      })
    }).catch(error => {
      res.send("Se ha producido un error al listar las canciones " + error)
    })
  });

  async function getWallet(user) {
    try {
      let filter = {email: user};
      let userObj = await usersRepository.findUser(filter, {});
      return userObj.wallet;
    } catch(error) {
      res.send("Se ha producido un error al obtener el monedero " + error);
    }
  }
}


