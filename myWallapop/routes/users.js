module.exports = function (app, usersRepository) {
  app.get('/users/signup', function (req, res) {
    res.render("signup.twig");
  });
  app.post('/users/signup', function (req, res) {
    //Validación en el servidor
    let responseFail = "/users/signup?message=";
    if (req.body.email === null || typeof (req.body.email) == 'undefined' || req.body.email.trim().length == 0)
      responseFail += "El email proporcionado no es válido\n";
    if (req.body.name === null || typeof (req.body.name) == 'undefined' || req.body.name.trim().length == 0)
      responseFail += "El nombre proporcionado no es válido\n";
    if (req.body.date === null || typeof (req.body.surname) == 'undefined' || req.body.surname.trim().length == 0)
      responseFail += "Los apellidos proporcionados no son válidos\n";
    const fechaValida = new Date();
    fechaValida.setFullYear(fechaValida.getFullYear() - 13);
    const fecha = new Date(req.body.date);
    if (req.body.date === null || typeof (req.body.date) == 'undefined' || fecha > fechaValida)
      responseFail += "La fecha no es válida\n";
    const passwordValidationMessage = validatePassword(req.body.password);
    if (passwordValidationMessage !== "")
      responseFail += passwordValidationMessage;
    if (req.body.passwordConfirm !== req.body.password)
      responseFail += "La confirmación de contraseña es distinta a la contraseña\n";
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
          let user = {
            email: req.body.email,
            name: req.body.name,
            surname: req.body.surname,
            birthDate: req.body.date,
            password: securePassword,
            wallet: 100,
            profile: "Usuario Estándar"
          }

          //Añadir usuario
          usersRepository.insertUser(user).then(userId => {
            // req.session.user = user.email;
            res.redirect("/users/userOffers" +
                "?message=Nuevo usuario registrado"+
                "&messageType=alert-info");
          }).catch(error => {
            res.redirect("/users/signup" +
                "?message=Se ha producido un error al registrar el usuario"+
                "&messageType=alert-danger");
          });
        }
      }).catch(error => {
        res.redirect("/users/login" +
            "?message=Se ha producido un error al buscar si había un usuario con ese email"+
            "&messageType=alert-danger ");
      });
    }
  });
}
function validatePassword(password) {
  if (password === null || typeof (password) == 'undefined' || password.trim().length == 0)
    return "La contraseña proporcionada no es válida, está vacía\n";
  if (!/[A-Z]/.test(password))
    return "La contraseña debe contener al menos una letra mayúscula\n";
  if (password.trim().length < 6)
    return "La contraseña debe tener al menos 6 caracteres\n";
  return "";
}
