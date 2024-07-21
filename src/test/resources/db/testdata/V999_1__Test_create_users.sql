insert into "user"(id, first_name, last_name, username, email, roles, github_id, avatar, pricing_method)
values ('joe_doe_id', 'Joe', 'Doe', 'JoeDoe', 'joe@email.com', '{USER}', '1234', 'https://github.com/images/JoeDoe.png',
        'TEN_MICRO'::pricing_method),('jane_doe_id', 'Jane', 'Doe', 'JaneDoe', 'jane@email.com', '{USER}', '5678', 'https://github.com/images/JaneDoe.png',
                                      'TEN_MICRO'::pricing_method);
