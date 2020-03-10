# Ktor ShareX
ShareX in Kotlin using Ktor

## Usage
```Kotlin
fun Application.module() {
	install(ShareX) {
		token = "verysecrettokenhere"
	}
	
	route("sharex") {
		shareXUpload("upload")
		shareXHost("get")
	}
	
	host(Regex("i\\..+")) {
		shareXHost()
	}
}
```
This will create an upload endpoint at example.com/sharex/upload, and hosting endpoints at both example.com/sharex/get and i.example.com


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
  "URL": "https://i.[websiteURL]/$response$"
}
```
