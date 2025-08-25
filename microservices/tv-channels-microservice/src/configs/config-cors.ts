import type { Express } from "express";
import cors from "cors";

export function addCors(server: Express) {
    server.use(cors({
        origin: ["http://localhost:3000", "https://myfrontend.com"],
        methods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
        maxAge: 300,
        credentials: true,
    }));
};