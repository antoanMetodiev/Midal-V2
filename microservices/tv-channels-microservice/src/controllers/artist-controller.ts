import { Router } from "express";
import { ArtistsService } from "../services/artists-service";

const router = Router();

router.get("/api/find-all", (req, res) => {

    try {
        console.log("===>>>>>>>>>> Title is =>>   " + req.body.title);
        const title: string = req.body.title;
        ArtistsService.findByTitleFromApi(title);
        res.sendStatus(201); // created!

    } catch(err) {
        console.log(err);
        res.sendStatus(500); // server error!
    };
});

export default router;