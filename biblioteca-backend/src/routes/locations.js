import express from 'express';
const router = express.Router();
import locationsController from '../controllers/locationsController.js';


router.get('/', locationsController.getLocations);

export default router;