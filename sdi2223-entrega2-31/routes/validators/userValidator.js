const {check} = require('express-validator');
const usersRepository = require("../../repositories/usersRepository");

exports.userSignUpValidator = [
    check("email", "El email proporcionado no es válido").trim().not().isEmpty(),
    check("email", "El email proporcionado es demasiado corto").trim().isLength({min: 3}),
    check("name", "El nombre proporcionado no es válido").trim().not().isEmpty(),
    check("name", "El nombre proporcionado es demasiado corto").trim().isLength({min: 3}),
    check("name", "El nombre proporcionado es demasiado largo").trim().isLength({max: 20}),
    check("surname", "Los apellidos proporcionados no son válidos").trim().not().isEmpty(),
    check("surname", "Los apellidos proporcionados son demasiado cortos").trim().isLength({min: 3}),
    check("surname", "El nombre proporcionado son demasiado largos").trim().isLength({max: 40}),
    check("password", "La contraseña proporcionada no es válida").trim().not().isEmpty(),
    check("password", "La contraseña debe tener al menos 6 caracteres").trim().isLength({min: 6}),
    check("passwordConfirm", "La contraseña proporcionada no es válida").trim().not().isEmpty(),
    check("passwordConfirm", "La contraseña debe tener al menos 6 caracteres").trim().isLength({min: 6}),
    check("passwordConfirm").custom((value, { req }) => {
        if (value !== req.body.password) {
            throw new Error("Las contraseñas no coinciden");
        }
        return true;
    }),
    check("date").custom( date => {
        const fechaValida = new Date();
        fechaValida.setFullYear(fechaValida.getFullYear() - 13);
        if (date > fechaValida){
            throw new Error("La fecha no es válida, debe tener más de 13 años");
        }
        return true;
    }),
    check("email").custom( async email => {
        let filter = {
            email: email
        }
        let options = {};
        const user = await usersRepository.findUser(filter, options);
        if (user != null) {
            throw new Error("Este correo ya está en uso");
        }
        return true;
    })
]

exports.userLoginValidator = [
    check("email", "El email proporcionado no es válido").trim().not().isEmpty(),
    check("email", "El email proporcionado es demasiado corto").trim().isLength({min: 3}),
    check("password", "La contraseña proporcionada no es válida").trim().not().isEmpty(),
    check("password", "La contraseña debe tener al menos 3 caracteres").trim().isLength({min: 3})
]