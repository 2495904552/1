package indi.mobilegear.common.items;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRadio extends Item {
  public EnumAction getItemUseAction(ItemStack stack) {
    return EnumAction.NONE;
  }
  
  public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    return EnumActionResult.PASS;
  }
  
  public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
    if (oldStack.hasTagCompound() && oldStack.getTagCompound().getInteger("Battery") != 200 && newStack
      .hasTagCompound() && newStack.getTagCompound().getInteger("Battery") == 200)
      return true; 
    return false;
  }
  
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
    NBTTagCompound nbt = stack.getTagCompound();
    boolean state = false;
    if (nbt != null && nbt.hasKey("Switch"))
      state = nbt.getBoolean("Switch"); 
    if (nbt != null && nbt.hasKey("Battery")) {
      int battery = nbt.getInteger("Battery");
      tooltip.add(((battery > 10 && state) ? "§2" : "") + (new TextComponentTranslation("mobilegear:electricity", new Object[0])).getFormattedText() + " " + battery);
    } else {
      tooltip.add((new TextComponentTranslation("mobilegear:electricity", new Object[0])).getFormattedText() + " 0");
    } 
    tooltip.add((new TextComponentTranslation("mobilegear:shift", new Object[0])).getFormattedText());
    tooltip.add((new TextComponentTranslation("mobilegear:chan", new Object[0])).getFormattedText());
  }
  
  public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
    return new ActionResult(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
  }
}
