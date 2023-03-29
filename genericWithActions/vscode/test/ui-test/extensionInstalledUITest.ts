import { expect } from 'chai';
import { ActivityBar, ExtensionsViewItem, ExtensionsViewSection, SideBarView, ViewControl } from 'vscode-extension-tester';
import { AdaptersConstants } from './common/constants/adaptersConstants';

/**
 * @author Oleksii Korniienko <olkornii@redhat.com>
 */
export function extensionInstalledUITest(): void {
  describe("Verify extension is vissible in installed list", () => {
    let view: ViewControl;
    let sideBar: SideBarView;
    let section: ExtensionsViewSection;

    beforeEach(async function () {
      this.timeout(10000);
      view = await new ActivityBar().getViewControl('Extensions');
      sideBar = await view.openView();
      const content = sideBar.getContent();
      section = (await content.getSection('Installed')) as ExtensionsViewSection;
    });

    it('RSP server community extension is installed', async function () {
      this.timeout(20000);
      const item = (await section.findItem(`@installed ` + AdaptersConstants.RSP_EXTENSTION_NAME)) as ExtensionsViewItem;
      expect(item).not.undefined;
      expect(await item.getTitle()).to.equal(AdaptersConstants.RSP_EXTENSTION_NAME);
    });

    afterEach(async function () {
      this.timeout(10000);
      section.clearSearch();
    });

    after(async () => {
      if (sideBar && (await sideBar.isDisplayed()) && view) {
        await view.closeView();
      }
    });
  });
}