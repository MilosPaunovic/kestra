<template>
    <div class="editor-wrapper">
        <div class="file-explorer">
            <h3>File Tree</h3>
            <ul>
                <li v-for="(file, index) in exampleFiles" :key="index" @click="loadFile(file)">
                    {{ file.name }}
                    <el-button type="danger" size="mini" @click.stop="deleteFile(index)">
                        Delete
                    </el-button>
                </li>
            </ul>
            <button @click="addNewFile">
                Add New File
            </button>
        </div>
        <div class="editor-container">
            <div v-for="(file, index) in openedFiles" :key="index" class="editor-tab">
                <span @click="switchFile(index)">{{ file.name }}</span>
                <el-button type="text" size="mini" @click.stop="closeFile(index)" class="close-button">
                    X
                </el-button>
            </div>
            <div ref="editorRef" class="editor" />
        </div>
    </div>
</template>

  <script>
    import {ref, onMounted, onBeforeUnmount} from "vue";
    import * as monaco from "monaco-editor";

    export default {
        name: "MonacoEditor",
        props: {
            initialContent: {
                type: String,
                default: "",
            },
        },
        setup(props) {
            const editorRef = ref(null);
            let editorInstance = null;
            const exampleFiles = ref([
                {name: "File1.js", content: "function exampleFunction1() {\n  // Function 1 body\n}"},
                {name: "File2.js", content: "function exampleFunction2() {\n  // Function 2 body\n}"},
                {name: "File3.js", content: "function exampleFunction3() {\n  // Function 3 body\n}"},
            ]);
            const openedFiles = ref([]);

            const loadFile = (file) => {
                if (!openedFiles.value.some((openedFile) => openedFile.name === file.name)) {
                    openedFiles.value.push(file);
                    editorInstance.setValue(file.content);
                }
            };

            const addNewFile = () => {
                const newName = `NewFile${openedFiles.value.length + 1}.js`;
                const newContent = "function newFunction() {\n  // New function body\n}";
                exampleFiles.value.push({name: newName, content: newContent});
                loadFile({name: newName, content: newContent});
            };

            const deleteFile = (index) => {
                exampleFiles.value.splice(index, 1);
                const openedFileIndex = openedFiles.value.findIndex((file) => file.name === exampleFiles.value[index].name);
                if (openedFileIndex !== -1) {
                    openedFiles.value.splice(openedFileIndex, 1);
                }
            };

            const switchFile = (index) => {
                const file = openedFiles.value[index];
                editorInstance.setValue(file.content);
            };

            const closeFile = (index) => {
                openedFiles.value.splice(index, 1);
                if (index === openedFiles.value.length) {
                    index--;
                }
                if (index >= 0) {
                    switchFile(index);
                } else {
                    editorInstance.setValue("");
                }
            };

            onMounted(() => {
                editorInstance = monaco.editor.create(editorRef.value, {
                    value: props.initialContent,
                    language: "javascript",
                });
            });

            onBeforeUnmount(() => {
                if (editorInstance) {
                    editorInstance.dispose();
                }
            });

            return {editorRef, exampleFiles, openedFiles, loadFile, addNewFile, deleteFile, switchFile, closeFile};
        },
    };
  </script>

  <style>
  .editor-wrapper {
    display: flex;
  }

  .file-explorer {
    flex: 0 0 200px;
    padding: 10px;
  }

  .file-explorer h3 {
    margin-top: 0;
  }

  .file-explorer ul {
    list-style: none;
    padding: 0;
  }

  .file-explorer li {
    display: flex;
    align-items: center;
    cursor: pointer;
    margin-bottom: 5px;
  }

  .file-explorer li button {
    margin-left: auto;
  }

  .editor-container {
    flex: 1;
    padding: 10px;
  }

  .editor-tab {
    display: inline-block;
    padding: 5px 10px;
    margin-right: 5px;
    cursor: pointer;
  }

  .editor-tab span {
    cursor: pointer;
  }

  .editor {
    width: 100%;
    height: 600px;
  }

  .close-button {
    color: red;
  }
  </style>
