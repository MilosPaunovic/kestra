<template>
    <div>
        <top-nav-bar :title="routeInfo.title">
            <template #additional-right>
                <namespace-select
                    class="fit-content"
                    data-type="flow"
                    :value="namespace"
                    @update:model-value="namespaceUpdate"
                    allow-create
                    :is-filter="false"
                />

                <el-dropdown>
                    <el-button :icon="Plus" class="p-2 m-0" />
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item :icon="FilePlus" @click="pickFile">
                                <input
                                    ref="filePicker"
                                    type="file"
                                    multiple
                                    style="display: none"
                                    @change="importFiles"
                                >
                                {{ $t("namespace files.import.file") }}
                            </el-dropdown-item>
                            <el-dropdown-item :icon="FolderPlus" @click="pickFolder">
                                <input
                                    ref="folderPicker"
                                    type="file"
                                    webkitdirectory
                                    mozdirectory
                                    msdirectory
                                    odirectory
                                    directory
                                    style="display: none"
                                    @change="importNSFiles"
                                >
                                {{ $t("namespace files.import.folder") }}
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
                <el-tooltip :hide-after="50" :content="$t('namespace files.export')">
                    <el-button :icon="FolderZip" class="p-2 m-0" @click="exportNsFiles" />
                </el-tooltip>
                <trigger-flow
                    ref="triggerFlow"
                    :disabled="!flow"
                    :flow-id="flow"
                    :namespace="namespace"
                />
            </template>
        </top-nav-bar>
        <div v-if="namespace" class="wrapper">
            <div class="sidebar">
                <div class="button">
                    <el-button type="primary" @click="fileDialog.visible = true">
                        Add File
                    </el-button>
                </div>
                <el-tree
                    :data="explorerFiles"
                    :expand-on-click-node="false"
                    @node-click="nodeClick"
                    empty-text="No Files"
                    class="sidebar"
                >
                    <template #default="{data}">
                        <el-row
                            class="row-bg"
                            justify="space-between"
                            :class="{opened: openedTabsNames.includes(data.label)}"
                        >
                            <el-col :span="6">
                                <span>
                                    {{ data.label }}
                                </span>
                            </el-col>
                            <el-col :span="6" class="remove-button">
                                <el-icon
                                    size="small"
                                    @click.stop="handleFileRemove(data.label)"
                                >
                                    <Delete />
                                </el-icon>
                            </el-col>
                        </el-row>
                    </template>
                </el-tree>
            </div>
            <el-tabs v-model="currentTab">
                <el-tab-pane
                    v-for="(tab, index) in openedTabs"
                    :key="index"
                    :name="index"
                    :label="tab.name"
                >
                    <template #label>
                        <el-text class="px-3">
                            {{ tab.name }}
                        </el-text>
                        <el-icon
                            size="large"
                            color="primary"
                            @click="handleTabClose(tab.name, index)"
                        >
                            <Close />
                        </el-icon>
                    </template>
                    <vue-monaco-editor
                        v-model:value="tab.content"
                        :options="EDITOR_OPTIONS"
                        :language="tab.language"
                        :theme="theme"
                    />
                </el-tab-pane>
            </el-tabs>

            <el-dialog
                v-model="fileDialog.visible"
                title="Add New File"
                width="500"
                @open-auto-focus="focusInputRef"
            >
                <el-input
                    ref="inputRef"
                    v-model="fileDialog.name"
                    placeholder="Name"
                    size="large"
                    class="mb-3"
                />
                <el-select
                    v-model="fileDialog.extension"
                    placeholder="Select"
                    size="large"
                    class="mb-3"
                >
                    <el-option
                        v-for="extension in extensions"
                        :key="extension.value"
                        :value="extension.value"
                        :label="extension.label"
                    />
                </el-select>
                <template #footer>
                    <div>
                        <el-button @click="fileDialog.visible = false">
                            Cancel
                        </el-button>
                        <el-button
                            type="primary"
                            :disabled="!fileDialog.name || !fileDialog.extension"
                            @click="addFile"
                        >
                            Add File
                        </el-button>
                    </div>
                </template>
            </el-dialog>
        </div>
        <section v-else class="container">
            <el-alert type="info" :closable="false">
                {{ $t("namespace choice") }}
            </el-alert>
        </section>
    </div>
</template>

<script setup>
    import {onUnmounted, watchEffect, nextTick, ref, computed} from "vue";

    import {useMonaco} from "@guolao/vue-monaco-editor";

    import NamespaceSelect from "./NamespaceSelect.vue";
    import TopNavBar from "../layout/TopNavBar.vue";
    import TriggerFlow from "../flows/TriggerFlow.vue";

    import Plus from "vue-material-design-icons/Plus.vue";
    import FolderPlus from "vue-material-design-icons/FolderPlus.vue";
    import FilePlus from "vue-material-design-icons/FilePlus.vue";
    import FolderZip from "vue-material-design-icons/FolderZip.vue";
    import Close from "vue-material-design-icons/Close.vue";
    import Delete from "vue-material-design-icons/Delete.vue";

    import {useI18n} from "vue-i18n";
    const {t} = useI18n();

    import {useToast} from "./composables/toast.js";
    const {success, error} = useToast();

    const EDITOR_OPTIONS = {
        automaticLayout: true,
        minimap: {enabled: false},
        formatOnType: true,
        formatOnPaste: true,
    };

    const containerRef = ref();
    const {monacoRef, unload} = useMonaco();

    const stop = watchEffect(() => {
        if (monacoRef.value && containerRef.value) {
            nextTick(() => stop());
            monacoRef.value.editor.create(containerRef.value, {});
        }
    });

    onUnmounted(() => !monacoRef.value && unload());

    const importedFiles = ref([]);
    const explorerFiles = computed(() =>
        importedFiles.value.map((file) => ({
            label: file.name,
            extension: file.extension,
            content: file.content,
        }))
    );
    const readFile = (file) => {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result);
            reader.onerror = reject;
            reader.readAsText(file);
        });
    };
    const importFiles = async (event) => {
        const files = [...event.target.files];

        try {
            for (const file of files) {
                const content = await readFile(file);
                importedFiles.value.push({
                    name: file.name,
                    extension: file.name.split(".").pop(),
                    content: content,
                });
            }
            success(t("namespace files.import.success"));
        } catch (_error) {
            error(t("namespace files.import.error"));
        } finally {
            event.target.value = "";
        }
    };

    const inputRef = ref();
    const focusInputRef = () => nextTick(() => inputRef.value.focus());
    const fileDialog = ref({visible: false, name: "", extension: ""});
    const getLanguage = (extension) => extension === "js" ? "javascript" : extension;
    const extensions = [
        {label: "JavaScript", value: "js"},
        {label: "YAML", value: "yaml"},
    ];
    const addFile = () => {
        const {name, extension} = fileDialog.value;
        const content = `// Initial content of your ${name}.${extension} file`;

        importedFiles.value.push({
            name: `${name}.${extension}`,
            extension,
            content,
        });
        fileDialog.value = {visible: false, name: "", extension: ""};

        nextTick(() => {
            openedTabs.value.push({
                name: `${name}.${extension}`,
                language: getLanguage(extension),
                content: content,
            });
            currentTab.value = openedTabs.value.length - 1;
        });
    };

    const nodeClick = (node) => {
        if (openedTabs.value.find((tab) => tab.name === node.label)) {
            currentTab.value = openedTabs.value.findIndex(
                (tab) => tab.name === node.label
            );
            return;
        }

        openedTabs.value.push({
            name: node.label,
            language: getLanguage(node.extension),
            content: node.content,
        });

        currentTab.value = openedTabs.value.findIndex(
            (tab) => tab.name === node.label
        );
    };
    const handleFileRemove = (name) => {
        openedTabs.value = openedTabs.value.filter((tab) => tab.name !== name);
        importedFiles.value = importedFiles.value.filter(
            (file) => file.name !== name
        );
    };

    const openedTabs = ref([]);
    const openedTabsNames = computed(() => openedTabs.value.map((tab) => tab.name));
    const currentTab = ref(0);
    const handleTabClose = (name, index) => {
        openedTabs.value = openedTabs.value.filter((tab) => tab.name !== name);
    };
</script>

<style lang="scss">
.wrapper {
  display: flex;
  height: calc(100vh - 64.8px);
}

.sidebar {
  width: 260px;
  overflow: auto;
  background-color: var(--card-bg);
  border-right: 1px solid var(--border-color);

  .button {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin: 20px;

    & button {
      width: 100%;
    }
  }

  .el-tree {
    --el-tree-node-hover-bg-color: var(--el-color-primary-light-8);

    & .el-tree-node {
      height: 34px;

      & .el-tree-node__content {
        height: 34px;

        & .el-row {
          width: 100%;

          &.opened {
            color: var(--el-color-primary);
          }

          & .remove-button {
            color: var(--el-color-primary);
            padding-right: 16px;
            text-align: right;
          }
        }
      }
    }
  }
}

.el-tabs {
  width: 1px;
  display: flex;
  flex-direction: column;
  flex: 1;

  .el-tabs__content {
    flex: 1;

    .el-tab-pane {
      height: 100%;
    }
  }
}
</style>

<script>
    import RouteContext from "../../mixins/routeContext";
    import RestoreUrl from "../../mixins/restoreUrl";
    import {apiUrl} from "override/utils/route";
    import {mapState} from "vuex";
    import {storageKeys} from "../../utils/constants";

    export default {
        mixins: [RouteContext, RestoreUrl],
        methods: {
            importNsFiles(event) {
                const files = [...event.target.files];
                Promise.all(
                    files.map((file) => {
                        return this.$store.dispatch("namespace/importFile", {
                            namespace: this.namespace,
                            file: file,
                        });
                    })
                )
                    .then(() => {
                        this.$message({
                            message: this.$t("namespace files.import.success"),
                            type: "success",
                        });
                    })
                    .catch(() => {
                        this.$message({
                            message: this.$t("namespace files.import.error"),
                            type: "error",
                        });
                    })
                    .finally(() => {
                        // this.$refs.vscodeIde.contentDocument.location.reload(true);
                        console.log("dsf");
                        event.target.value = "";
                    });
            },
            exportNsFiles() {
                this.$store.dispatch("namespace/exportFiles", {
                    namespace: this.namespace,
                });
            },
            pickFile() {
                this.$refs.filePicker.click();
            },
            pickFolder() {
                this.$refs.folderPicker.click();
            },
            namespaceUpdate(namespace) {
                localStorage.setItem(storageKeys.LATEST_NAMESPACE, namespace);
                this.$router.push({
                    params: {
                        namespace,
                    },
                });
            },
            handleTabsDirty(tabs) {
                // Add tabs not saved
                this.tabsNotSaved = this.tabsNotSaved.concat(tabs.dirty);
                // Removed tabs closed
                this.tabsNotSaved = this.tabsNotSaved.filter(
                    (e) => !tabs.closed.includes(e)
                );
                this.$store.dispatch("core/isUnsaved", this.tabsNotSaved.length > 0);
            },
        },
        data() {
            return {
                flow: null,
                tabsNotSaved: [],
                uploadFileName: undefined,
            };
        },
        mounted() {
            window.addEventListener("message", (event) => {
                const message = event.data;
                if (message.type === "kestra.tabFileChanged") {
                    const flowsFolderPath = `/${this.namespace}/_flows/`;
                    const filePath = message.filePath.path;
                    if (filePath.startsWith(flowsFolderPath)) {
                        const fileName = filePath.split(flowsFolderPath)[1];
                        // trim the eventual extension
                        this.flow = fileName.split(".")[0];
                    } else {
                        this.flow = undefined;
                    }
                } else if (message.type === "kestra.tabsChanged") {
                    this.handleTabsDirty(message.tabs);
                } else if (message.type === "kestra.flowSaved") {
                    this.$refs.triggerFlow.loadDefinition();
                }
            });

            // Setup namespace
            const namespace = localStorage.getItem(storageKeys.LATEST_NAMESPACE)
                ? localStorage.getItem(storageKeys.LATEST_NAMESPACE)
                : localStorage.getItem(storageKeys.DEFAULT_NAMESPACE);
            if (namespace) {
                this.namespaceUpdate(namespace);
            } else if (this.namespaces?.length > 0) {
                this.namespaceUpdate(this.namespaces[0]);
            } else if (localStorage.getItem("tourDoneOrSkip") !== "true") {
                this.$router.push({
                    name: "flows/create",
                    params: {
                        tenant: this.$route.params.tenant,
                    },
                });
            }
        },
        computed: {
            ...mapState("namespace", ["namespaces"]),
            routeInfo() {
                return {
                    title: this.$t("editor"),
                };
            },
            theme() {
                const current = localStorage.getItem("theme");
                if (!current) return "vs";

                return current === "light" ? "vs" : "vs-dark";
            },
            vscodeIndexUrl() {
                const uiSubpath = KESTRA_UI_PATH === "./" ? "/" : KESTRA_UI_PATH;
                return `${uiSubpath}vscode.html?KESTRA_UI_PATH=${uiSubpath}&KESTRA_API_URL=${apiUrl(
                    this.$store
                )}&THEME=${this.theme}&namespace=${this.namespace}`;
            },
            namespace() {
                return this.$route.params.namespace;
            },
        },
    };
</script>

<style lang="scss">
.fit-content {
  width: fit-content;
}
.vscode-editor {
  height: calc(100vh - 64.8px) !important;
  width: 100%;
}
</style>