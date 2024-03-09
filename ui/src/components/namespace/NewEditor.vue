<template>
    <div>
        <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
            <el-tab-pane label="User" name="first">
                User
            </el-tab-pane>
            <el-tab-pane label="Config" name="second">
                Config
            </el-tab-pane>
            <el-tab-pane label="Role" name="third">
                Role
            </el-tab-pane>
            <el-tab-pane label="Task" name="fourth">
                Task
            </el-tab-pane>
        </el-tabs>
        <div class="d-flex">
            <el-button @click="addFile">
                <span>Add File</span>
            </el-button>
            <el-button @click="removeFile">
                <span>Remove File</span>
            </el-button>
            <el-tree
                :data="data"
                @node-click="handleClick"
            />
            <code-mirror
                v-model="value"
                :lang="lang"
                :linter="linter"
                style="width: 100%;"
            />
        </div>
    </div>
</template>

  <script>
    import {ref, defineComponent} from "vue";
    import CodeMirror from "vue-codemirror6";
    import {json, jsonParseLinter} from "@codemirror/lang-json";

    export default defineComponent({
        components: {
            CodeMirror,
        },
        setup() {
            const value = ref(`
                {
                    "glossary": {
                        "title": "example glossary",
                        "GlossDiv": {
                            "title": "S",
                            "GlossList": {
                                "GlossEntry": {
                                    "ID": "SGML",
                                    "SortAs": "SGML",
                                    "GlossTerm": "Standard Generalized Markup Language",
                                    "Acronym": "SGML",
                                    "Abbrev": "ISO 8879:1986",
                                    "GlossDef": {
                                        "para": "A meta-markup language, used to create markup languages such as DocBook.",
                                        "GlossSeeAlso": ["GML", "XML"]
                                    },
                                    "GlossSee": "markup"
                                }
                            }
                        }
                    }
                }`);


            const activeName = ref("first")

            const handleClick = (tab, event) => {
                console.log(tab, event)
            }


            const lang = json();
            const linter = jsonParseLinter();

            let data = ref([
                {
                    label: "Level one 1",
                    children: [
                        {
                            label: "Level two 1-1",
                            children: [
                                {
                                    label: "Level three 1-1-1",
                                },
                            ],
                        },
                    ],
                },
                {
                    label: "Level one 2",
                    children: [
                        {
                            label: "Level two 2-1",
                            children: [
                                {
                                    label: "Level three 2-1-1",
                                },
                            ],
                        },
                        {
                            label: "Level two 2-2",
                            children: [
                                {
                                    label: "Level three 2-2-1",
                                },
                            ],
                        },
                    ],
                },
                {
                    label: "Level one 3",
                    children: [
                        {
                            label: "Level two 3-1",
                            children: [
                                {
                                    label: "Level three 3-1-1",
                                },
                            ],
                        },
                        {
                            label: "Level two 3-2",
                            children: [
                                {
                                    label: "Level three 3-2-1",
                                },
                            ],
                        },
                    ],
                },
            ])

            const addFile = () => {
                data.value.push({
                    label: `Level ${Math.floor(Math.random() * 90 + 10)}`,
                })
            }

            const removeFile = () => {
                data.value.splice(0, 1)
            }

            return {value, data, addFile, removeFile,  lang, linter, activeName, handleClick};
        },
    });
  </script>

  <style>
  .demo-tabs > .el-tabs__content {
    padding: 32px;
    color: #6b778c;
    font-size: 32px;
    font-weight: 600;
  }
  </style>