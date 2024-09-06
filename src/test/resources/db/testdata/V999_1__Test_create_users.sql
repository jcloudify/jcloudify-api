insert into "user"(id, first_name, last_name, username, email, roles, github_id, avatar, pricing_method, stripe_id)
values ('joe_doe_id', 'Joe', 'Doe', 'JoeDoe', 'joe@email.com', '{USER}', '1234', 'https://github.com/images/JoeDoe.png',
        'TEN_MICRO'::pricing_method, 'joe_stripe_id'),('jane_doe_id', 'Jane', 'Doe', 'JaneDoe', 'jane@email.com', '{USER}', '5678', 'https://github.com/images/JaneDoe.png',
                                      'TEN_MICRO'::pricing_method, 'jane_stripe_id'),
       ('denis_ritchie_id', 'Denis', 'Ritchie', 'DenisRitchie', 'denis@email.com', '{USER}', '1010', 'https://github.com/images/DenisRitchie.png',
        'TEN_MICRO'::pricing_method, 'denis_ritchie_stripe_id');
