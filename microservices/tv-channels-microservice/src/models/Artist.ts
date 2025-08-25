import { UUID } from "crypto";

export type Artist = {
    id: UUID;
    mb_id: string;
    name: string;
    country?: string;
    gender?: string;
    yearFormed?: number;
    summary?: string;
    thumbnails?: string[];
    backgrounds?: string[];
    facebook?: string;
    twitter?: string;
    instagram?: string;
    officialWebsite?: string;
    lastUpdated: Date;
};