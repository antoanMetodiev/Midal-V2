import server from "./configs/config-express";
import { declareRoutes } from "./routes";

declareRoutes(server);

// Heartbeat Request:
server.get("/heartbeat", (req, res) => {
    console.log("=====>>>>>>>> Heart Beat Received...!!!");
    res.send("Heart Beat Received!");
});

const PORT = 5000;
server.listen(PORT, () => {
    console.log("Server is starting on PORT - 5000!");
});