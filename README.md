# Ktor ShareX
ShareX in Kotlin using Ktor

## Usage
```Kotlin
fun Application.module() {
	install(ShareX) {
		token = "verysecrettokenhere"
	}
	
	install(Routing) {
		shareX("sharex")
	}
}
```

ShareX config:
```Json
{
  "Version": "12.4.1",
  "Name": "KtorShareX",
  "DestinationType": "ImageUploader, TextUploader, FileUploader",
  "RequestMethod": "POST",
  "RequestURL": "https://[websiteURL]/sharex/upload",
  "Body": "MultipartFormData",
  "Arguments": {
    "token": "verysecrettokenhere"
  },
  "FileFormName": "file",
  "URL": "https://[websiteURL]/$response$"
}
```
