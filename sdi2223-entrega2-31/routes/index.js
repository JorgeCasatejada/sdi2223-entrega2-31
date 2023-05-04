var express = require('express');
var router = express.Router();
const log4js = require('log4js');

/* GET home page. */
router.get('/', async function(req, res, next) {
  res.redirect('/myWallapop');
});

module.exports = router;
