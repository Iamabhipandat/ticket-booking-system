-- ========================================================
-- Ticket Booking Database Seeder: Indian & Global Blockbusters
-- ========================================================

-- 1. Insert Theatres
INSERT INTO theatres (name, city, address)
VALUES 
  ('PVR INOX Pacific Mall', 'Delhi', 'Tagore Garden, Najafgarh Road, New Delhi'),
  ('Cinepolis Vegas Mall', 'Delhi', 'Sector 14, Dwarka, New Delhi'),
  ('Wave Cinemas Silver City', 'Noida', 'Sector 18, Commercial Belt, Noida')
ON CONFLICT DO NOTHING;

-- 2. Insert Screens (Screen 1 & Screen 2 for each theatre)
DO $$
DECLARE
    t RECORD;
BEGIN
    FOR t IN SELECT id FROM theatres LOOP
        INSERT INTO screens (screen_number, total_seats, theatre_id)
        VALUES 
            (1, 24, t.id),
            (2, 24, t.id)
        ON CONFLICT (theatre_id, screen_number) DO NOTHING;
    END LOOP;
END $$;

-- 3. Insert Seats for all screens (3 rows of 8 seats: A=REGULAR, B=PREMIUM, C=RECLINER)
DO $$
DECLARE
    s RECORD;
    seat_num INT;
BEGIN
    FOR s IN SELECT id FROM screens LOOP
        -- Row A: REGULAR (A1..A8)
        FOR seat_num IN 1..8 LOOP
            INSERT INTO seats (row_name, seat_number, seat_type, screen_id)
            VALUES ('A', seat_num, 'REGULAR', s.id)
            ON CONFLICT (screen_id, row_name, seat_number) DO NOTHING;
        END LOOP;

        -- Row B: PREMIUM (B1..B8)
        FOR seat_num IN 1..8 LOOP
            INSERT INTO seats (row_name, seat_number, seat_type, screen_id)
            VALUES ('B', seat_num, 'PREMIUM', s.id)
            ON CONFLICT (screen_id, row_name, seat_number) DO NOTHING;
        END LOOP;

        -- Row C: RECLINER (C1..C8)
        FOR seat_num IN 1..8 LOOP
            INSERT INTO seats (row_name, seat_number, seat_type, screen_id)
            VALUES ('C', seat_num, 'RECLINER', s.id)
            ON CONFLICT (screen_id, row_name, seat_number) DO NOTHING;
        END LOOP;
    END LOOP;
END $$;

-- 4. Insert Movies: Ramayana, Pushpa 2, Bhool Bhulaiyaa 3, Singham Again, Kalki 2898 AD, Stree 2, Gladiator II, Dune 2
INSERT INTO movies (title, description, duration_minutes, language, genre, poster_url, rating)
VALUES
(
  'Ramayana: Part 1',
  'The epic saga of Lord Rama, Princess Sita, and Lakshmana during their exile in Dandakaranya, the abduction of Sita by Demon King Ravana of Lanka, and the righteous alliance with Lord Hanuman and the Vanara army to restore cosmic Dharma.',
  178,
  'Hindi',
  'Mythological / Action / Epic',
  'https://images.unsplash.com/photo-1544717305-2782549b5136?w=700&auto=format&fit=crop&q=80',
  9.6
),
(
  'Pushpa 2: The Rule',
  'Pushpa Raj continues to consolidate his invincible grip over the red sandalwood empire. When SP Bhanwar Singh Shekhawat launches a venomous counter-offensive, Pushpa prepares for an explosive confrontation across interstate borders.',
  195,
  'Hindi',
  'Action / Crime / Drama',
  'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=700&auto=format&fit=crop&q=80',
  9.2
),
(
  'Bhool Bhulaiyaa 3',
  'Rooh Baba journeys to the sinister royal palace of Rakhtghat in West Bengal to solve an ancient haunting, only to unleash chaos when two menacing spirits step forward claiming to be the wrathful Manjulika.',
  158,
  'Hindi',
  'Comedy / Horror',
  'https://images.unsplash.com/photo-1509248961158-e54f6934749c?w=700&auto=format&fit=crop&q=80',
  8.5
),
(
  'Singham Again',
  'DCP Bajirao Singham embarks on a dangerous cross-border mission to rescue his kidnapped wife Avni. Uniting with Simmba, Sooryavanshi, ACP Satya, and DCP Shakti Shetty, the Cop Universe prepares for relentless warfare.',
  144,
  'Hindi',
  'Action / Masala / Thriller',
  'https://images.unsplash.com/photo-1574267432553-4b4628081c31?w=700&auto=format&fit=crop&q=80',
  8.3
),
(
  'Kalki 2898 AD',
  'Set in a post-apocalyptic dystopia of Kasi in the year 2898 AD, the legendary immortal Ashwatthama awakens to protect SUM-80 and the divine child she carries from Supreme Yaskin bounty hunters, led by the carefree mercenary Bhairava.',
  181,
  'Hindi',
  'Sci-Fi / Action / Epic',
  'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=700&auto=format&fit=crop&q=80',
  8.9
),
(
  'Stree 2: Sarkate Ka Aatank',
  'The eccentric town of Chanderi is terrorized once more by a horrifying headless phantom named Sarkata who kidnaps progressive women. Vicky, Bittu, Janna, and Rudra team up with the elusive mystery girl and Stree herself to vanquish the darkness.',
  147,
  'Hindi',
  'Comedy / Horror',
  'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=700&auto=format&fit=crop&q=80',
  8.7
),
(
  'Gladiator II',
  'Years after witnessing the death of Maximus at the hands of his uncle, Lucius is forced to enter the Colosseum after his home is conquered by tyrannical Roman emperors, fighting to return honor to Rome.',
  148,
  'English',
  'Action / Adventure / Drama',
  'https://images.unsplash.com/photo-1440404653325-ab127d49abc1?w=700&auto=format&fit=crop&q=80',
  8.4
),
(
  'Dune: Part Two',
  'Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family. Facing a choice between the love of his life and the fate of the known universe, he endeavors to prevent a terrible future.',
  166,
  'English',
  'Sci-Fi / Adventure',
  'https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=700&auto=format&fit=crop&q=80',
  8.8
);

-- 5. Schedule Shows for All Movies across Screens
-- Schedules shows for today and tomorrow with various show slots
DO $$
DECLARE
    m RECORD;
    s RECORD;
    today_date DATE := CURRENT_DATE;
    slot_start TIMESTAMP;
    slot_end TIMESTAMP;
    new_show_id BIGINT;
    seat_rec RECORD;
    seat_price NUMERIC(10,2);
BEGIN
    FOR m IN SELECT id, duration_minutes FROM movies LOOP
        -- Pick a screen for this movie
        FOR s IN SELECT id FROM screens ORDER BY id LIMIT 3 LOOP
            
            -- Slot 1: Today 11:00 AM
            slot_start := today_date + TIME '11:00:00';
            slot_end := slot_start + (m.duration_minutes || ' minutes')::INTERVAL;
            INSERT INTO shows (movie_id, screen_id, start_time, end_time, status, ticket_price)
            VALUES (m.id, s.id, slot_start, slot_end, 'SCHEDULED', 250.00)
            RETURNING id INTO new_show_id;

            -- Auto-initialize show_seats for Slot 1
            FOR seat_rec IN SELECT id, seat_type FROM seats WHERE screen_id = s.id LOOP
                seat_price := CASE 
                    WHEN seat_rec.seat_type = 'RECLINER' THEN 400.00
                    WHEN seat_rec.seat_type = 'PREMIUM' THEN 300.00
                    ELSE 250.00
                END;
                INSERT INTO show_seats (show_id, seat_id, price, status)
                VALUES (new_show_id, seat_rec.id, seat_price, 'AVAILABLE')
                ON CONFLICT (show_id, seat_id) DO NOTHING;
            END LOOP;

            -- Slot 2: Today 03:30 PM
            slot_start := today_date + TIME '15:30:00';
            slot_end := slot_start + (m.duration_minutes || ' minutes')::INTERVAL;
            INSERT INTO shows (movie_id, screen_id, start_time, end_time, status, ticket_price)
            VALUES (m.id, s.id, slot_start, slot_end, 'SCHEDULED', 280.00)
            RETURNING id INTO new_show_id;

            FOR seat_rec IN SELECT id, seat_type FROM seats WHERE screen_id = s.id LOOP
                seat_price := CASE 
                    WHEN seat_rec.seat_type = 'RECLINER' THEN 420.00
                    WHEN seat_rec.seat_type = 'PREMIUM' THEN 320.00
                    ELSE 280.00
                END;
                INSERT INTO show_seats (show_id, seat_id, price, status)
                VALUES (new_show_id, seat_rec.id, seat_price, 'AVAILABLE')
                ON CONFLICT (show_id, seat_id) DO NOTHING;
            END LOOP;

            -- Slot 3: Today 07:45 PM (Prime Evening)
            slot_start := today_date + TIME '19:45:00';
            slot_end := slot_start + (m.duration_minutes || ' minutes')::INTERVAL;
            INSERT INTO shows (movie_id, screen_id, start_time, end_time, status, ticket_price)
            VALUES (m.id, s.id, slot_start, slot_end, 'SCHEDULED', 350.00)
            RETURNING id INTO new_show_id;

            FOR seat_rec IN SELECT id, seat_type FROM seats WHERE screen_id = s.id LOOP
                seat_price := CASE 
                    WHEN seat_rec.seat_type = 'RECLINER' THEN 500.00
                    WHEN seat_rec.seat_type = 'PREMIUM' THEN 400.00
                    ELSE 350.00
                END;
                INSERT INTO show_seats (show_id, seat_id, price, status)
                VALUES (new_show_id, seat_rec.id, seat_price, 'AVAILABLE')
                ON CONFLICT (show_id, seat_id) DO NOTHING;
            END LOOP;

            -- Slot 4: Tomorrow 06:00 PM
            slot_start := (today_date + 1) + TIME '18:00:00';
            slot_end := slot_start + (m.duration_minutes || ' minutes')::INTERVAL;
            INSERT INTO shows (movie_id, screen_id, start_time, end_time, status, ticket_price)
            VALUES (m.id, s.id, slot_start, slot_end, 'SCHEDULED', 300.00)
            RETURNING id INTO new_show_id;

            FOR seat_rec IN SELECT id, seat_type FROM seats WHERE screen_id = s.id LOOP
                seat_price := CASE 
                    WHEN seat_rec.seat_type = 'RECLINER' THEN 450.00
                    WHEN seat_rec.seat_type = 'PREMIUM' THEN 350.00
                    ELSE 300.00
                END;
                INSERT INTO show_seats (show_id, seat_id, price, status)
                VALUES (new_show_id, seat_rec.id, seat_price, 'AVAILABLE')
                ON CONFLICT (show_id, seat_id) DO NOTHING;
            END LOOP;

        END LOOP;
    END LOOP;
END $$;
