import axios from "axios";
import { Artist } from "../models/Artist";
import { randomUUID } from "crypto";
import sleep from "../utils/sleep-function";

const FANART_API_KEY = process.env.FANART_API_KEY;

export const ArtistsService = {

    findByTitleFromApi: async (title: string) => {
        title = title.replace(/\s+/g, "+");

        // 1️. MusicBrainz API:
        let response = await axios.get(
            `https://musicbrainz.org/ws/2/artist/?query=artist:${encodeURIComponent(title.trim())}&fmt=json`,
            { headers: { "User-Agent": "MyMusicApp/1.0 (email@example.com)" } }
        );

        const mbArtists = response.data.artists || [];
        if (mbArtists.length === 0) return [];

        for (const mbData of mbArtists) {
            try {
                // 2. Fanart.tv API:
                const fanartResponse = await axios.get(
                    `https://webservice.fanart.tv/v3/music/${mbData.id}?api_key=${FANART_API_KEY}`
                );
                const fanartData = fanartResponse.data;

                // 3. Wikipedia API:
                const wikiTitle = mbData.name.trim().replace(/\s+/g, "_");
                const wikiResponse = await axios.get(
                    `https://en.wikipedia.org/api/rest_v1/page/summary/${encodeURIComponent(wikiTitle)}`
                );

                const thumbnails = fanartData.artistthumb?.map((img: any) => img.url) || [];
                const backgrounds = fanartData.artistbackground?.map((img: any) => img.url) || [];

                const artist: Artist = {
                    id: randomUUID(),
                    mb_id: mbData.id,
                    name: mbData.name,
                    country: mbData.area?.name,
                    gender: mbData.gender,
                    yearFormed: mbData["life-span"]?.begin ? parseInt(mbData["life-span"].begin) : 2015,
                    summary: wikiResponse.data.extract,
                    thumbnails,
                    backgrounds,
                    lastUpdated: new Date()
                };

                saveArtistToDB(artist);
                await sleep(1000); // Sleep for 1sec.

            } catch (err) {
                console.error(`Error fetching data for artist ${mbData.name}:`, err);
            };
        };
    },
};

async function saveArtistToDB(artist: Artist) {
    // Save to Database:


    console.dir(artist, { depth: null });
};