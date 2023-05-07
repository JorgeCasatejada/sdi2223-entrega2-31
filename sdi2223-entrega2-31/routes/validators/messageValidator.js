const {check} = require('express-validator');

exports.firstMessageValidator = [
    check("text", "El primer mensaje no puede estar vacío").trim().not().isEmpty()
]