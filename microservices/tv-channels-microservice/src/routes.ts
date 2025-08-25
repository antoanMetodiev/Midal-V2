import type { Express } from "express";

import artistController from "./controllers/artist-controller";

export function declareRoutes(server: Express) {
    server.use("/artist", artistController);
};