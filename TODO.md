1. Fix issues;
2. At startup the bot should check if people in the DB left or new people joined;
3. `user.here` should be a date of when the user left, null if the user is inside, then the user is deleted after a
year;
4. Move all output texts to an enum to avoid duplicates.