import express from "express";
import { addCors } from "./config-cors";
import dotenv from "dotenv";

const server = express();

dotenv.config();
addCors(server);
server.use(express.json());

export default server;