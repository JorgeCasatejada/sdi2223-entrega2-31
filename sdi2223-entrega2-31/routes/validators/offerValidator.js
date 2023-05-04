const {check} = require("express-validator");

exports.offerAddValidator = [
    check("title", "El título proporcionado no es válido").trim().not().isEmpty(),
    check("title", "El título proporcionado es demasiado corto").trim().isLength({min: 3}),
    check("description", "La descripción proporcionada no es válida").trim().not().isEmpty(),
    check("description", "La descripción proporcionada es demasiado corta").trim().isLength({min: 3}),
    check("price", "El precio proporcionado no es válido, debe ser un número").isNumeric(),
    check("price").custom(value => {
        if (value <= 0) {
            throw new Error("El precio proporcionado no es válido, debe ser positivo");
        }
        return true;
    })
]