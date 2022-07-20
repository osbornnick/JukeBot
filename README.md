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
}

session {
	name
	owner
	playlist_id
	queue (first is currently playing?)
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
