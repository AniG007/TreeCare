//Update path variables
process.env.GCLOUD_PROJECT = 'smooth-pivot-242815'
process.env.FIREBASE_CONFIG = {
	databaseURL: 'https://smooth-pivot-242815.firebaseio.com',
	storageBucket: 'smooth-pivot-242815.appspot.com',
	projectId: 'projectId'
}

//Function code starts

const functions = require('firebase-functions')
const admin = require('firebase-admin')
    
admin.initializeApp({
	apiKey: 'AIzaSyC8TIk-qpdJ_UFvmqP_J4HeBzf_MLDnn4o' /*API Key*/, 
	authDomain: 'smooth-pivot-242815.firebaseapp.com',
	projectId: 'smooth-pivot-242815',
	databaseURL: "https://smooth-pivot-242815.firebaseio.com"
})

const db = admin.firestore()

exports.updateTournamentDaily = functions.https.onRequest((req, res) => {

	db.collection('tournaments').get().then((querySnapshot) => {
			querySnapshot.forEach((doc) => {
				console.log(`${doc.id} => ${doc.data()}`);

				const tournament = doc.data();

				const startDate = tournament.startTimestamp.toDate().getDate();
				const startMonth = tournament.startTimestamp.toDate().getMonth();
				const startYear = tournament.startTimestamp.toDate().getFullYear();
				const endTime = tournament.finishTimestamp.toDate().getTime();

				const currentTime = new Date().getTime();
				const currentDate = new Date().getDate();
				const currentMonth = new Date().getMonth();
				const currentYear = new Date().getFullYear();

				if ((startDate === currentDate) && (startMonth === currentMonth) && (startYear === currentYear)) {
					db.collection('tournaments').doc(tournament.name)
						.set({
							active: true
						}, {merge: true})
						console.log(`Updated tournament: ${tournament.name}`);
					}

				if ((endTime < currentTime)) {
					db.collection('tournaments').doc(tournament.name)
						.set({
							active: false
						}, {merge: true})
					console.log(`Updated tournament: ${tournament.name}`);
				}
			})
			return true
		})
		.catch(error => {
			console.log('error', error);
		})
})