# JukeBot
Collaborative playlist making on Android



Additional Icon Source: Leaving Session Icon: https://www.flaticon.com/free-icons/logout

schema:
```
user {
	email
	spotify auth
	password
	username
	memberOf: []
}

session {
	join_code // ?
	name
	owner
	playlist_id
	queue (first is currently playing?)
	members: []
}

chats {
	session_id {
		chat {
			sentBy
			messageBody
			createdAt
			messageType
		}
	}
}

messeges {
	session_id
	user_sending
	text
	createdAt
	messageType
}

playlist {
	// ?
}
```
