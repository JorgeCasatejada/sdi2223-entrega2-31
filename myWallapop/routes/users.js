module.exports = function (app, usersRepository, offersRepository) {
  app.get('/myWallapop', function (req, res) {
    res.render("index.twig");
  });
  app.get('/users/signup', function (req, res) {
    res.render("signup.twig", {user: req.session.user});
  });
  app.post('/users/signup', function (req, res) {
    //Validación en el servidor
    let responseFail = "/users/signup?message=";
    if (req.body.email === null || typeof (req.body.email) == 'undefined' || req.body.email.trim().length == 0)
      responseFail += "El email proporcionado no es válido<br>";
    if (req.body.name === null || typeof (req.body.name) == 'undefined' || req.body.name.trim().length == 0)
      responseFail += "El nombre proporcionado no es válido<br>";
    if (req.body.date === null || typeof (req.body.surname) == 'undefined' || req.body.surname.trim().length == 0)
      responseFail += "Los apellidos proporcionados no son válidos<br>";
    const fechaValida = new Date();
    fechaValida.setFullYear(fechaValida.getFullYear() - 13);
    const fecha = new Date(req.body.date);
    if (req.body.date === null || typeof (req.body.date) == 'undefined' || fecha > fechaValida)
      responseFail += "La fecha no es válida, debe tener más de 13 años<br>";
    const passwordValidationMessage = validatePassword(req.body.password);
    if (passwordValidationMessage !== "")
      responseFail += passwordValidationMessage;
    if (req.body.passwordConfirm !== req.body.password)
      responseFail += "La confirmación de contraseña es distinta a la contraseña<br>";
    if (responseFail.length > 25){
      res.redirect(responseFail + "&messageType=alert-danger");
    } else {
      //Comprobar que no existe el usuario ya
      let filter = {
        email: req.body.email
      }
      let options = {};
      usersRepository.findUser(filter, options).then(user => {
        if (user != null){
          res.redirect("/users/signup" +
              "?message=Este correo ya está en uso"+
              "&messageType=alert-danger ");
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
            getWallet(req.session.user).then(wallet => {
              res.render("users/userOffers.twig", {user: req.session.user, wallet: wallet});
            }).catch(error => {
              res.send("Se ha producido un error al obtener el monedero " + error)
            })
          }).catch(error => {
            req.session.user = null;
            res.redirect("/users/signup" +
                "?message=Se ha producido un error al registrar el usuario"+
                "&messageType=alert-danger");
          });
        }
      }).catch(error => {
        req.session.user = null;
        res.redirect("/users/login" +
            "?message=Se ha producido un error al buscar si había un usuario con ese email"+
            "&messageType=alert-danger ");
      });
    }
  });
  app.get('/users/login', function (req, res) {
    res.render("login.twig", {user: req.session.user});
  });
  app.post('/users/login', function (req, res) {
    //Validar datos
    let responseFail = "/users/signup?message=";
    if (req.body.email === null || typeof (req.body.email) == 'undefined' || req.body.email.trim().length == 0)
      responseFail += "El email proporcionado no es válido<br>";
    if (req.body.password === null || typeof (req.body.password) == 'undefined' || req.body.password.trim().length == 0)
      responseFail += "La contraseña proporcionada no es válida<br>";
    if (responseFail.length > 25){
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
      usersRepository.findUser(filter, options).then(user => {
        if (user == null) {
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
          }
          else{
            res.redirect("/admin/users" +
                "?message=Inicio de sesión correcto (Administrador)" +
                "&messageType=alert-info ");
          }
        }
      }).catch(error => {
        req.session.user = null;
        res.redirect("/users/login" +
            "?message=Se ha producido un error al buscar el usuario" +
            "&messageType=alert-danger ");
      });
    }
  });
  app.get('/users/logout', function (req, res) {
    const prevUser = req.session.user != null ? true : false;
    req.session.user = null;
    // si habia un usuario logueado, imprimir el mensaje, si no no (acceder a traves de url)
    if (prevUser)
      res.redirect("/users/login" +
          "?message=El usuario se ha desconectado correctamente" +
          "&messageType=alert-info ");
    else{
      res.redirect("/users/login")
    }
  });
  app.get('/user/offers', function (req, res) {
    let filter = {author: req.session.user};
    let options = {sort: {title: 1}};

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
function validatePassword(password) {
  if (password === null || typeof (password) == 'undefined' || password.trim().length == 0)
    return "La contraseña proporcionada no es válida, está vacía<br>";
  if (password.trim().length < 6)
    return "La contraseña debe tener al menos 6 caracteres<br>";
  return "";
}


