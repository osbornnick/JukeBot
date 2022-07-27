# JukeBot
Collaborative playlist making on Android



Additional Icon Source: Leaving Session Icon: https://www.flaticon.com/free-icons/logout

schema:
```
users {
	user {
		spotify auth // ?
		username
		memberOf: [] // sessions
		settings // ?
	}
}

sessions {
	session_id {
		join_code // ?
		name
		owner
		playlist_id
		members: []
	}
}

queues {
	session_id {
		song_id { // song details from spotify?
			upvotes: [users]
			downvotes: [users]
			cumulative
		}
	}
}

chats {
	session_id {
		message_id {
			sentBy
			messageBody
			createdAt
			messageType
		}
	}
}

playlist {
	// ?
}
```
