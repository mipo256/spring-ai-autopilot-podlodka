import chromadb

client = chromadb.PersistentClient(path="./chroma_db")
collection_name = "spring-ai-autopilot"
collection = client.create_collection(name=collection_name)

print(f"Collection '{collection_name}' created successfully.")